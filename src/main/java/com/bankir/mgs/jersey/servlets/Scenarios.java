package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.SorterProperty;
import com.bankir.mgs.User;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.hibernate.dao.ScenarioChannelDAO;
import com.bankir.mgs.hibernate.dao.ScenarioDAO;
import com.bankir.mgs.infobip.InfobipMessageGateway;
import com.bankir.mgs.infobip.model.InfobipObjects;
import com.bankir.mgs.jersey.model.JsonObject;
import com.bankir.mgs.jersey.model.ScenarioObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервлет обрабатывает ссылки:
 * ../scenarios          POST    admin   создаёт новый сценарий
 * ../scenarios          PUT     admin   обновление сценария
 * ../scenarios/get   GET     admin   получение данных сценария
 * ../scenarios            GET     admin   получение списка сценариев
 */

@Path("/scenarios")
public class Scenarios extends BaseServlet {

    private String[] viewScenariosRoles = {User.ROLE_ADMIN, User.ROLE_READER, User.ROLE_EDITOR};
    private Gson gson = Config.getGsonBuilder().create();

    @POST
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonObject create(ScenarioObject scenario) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        /* Если имя сценария не задали, то выводим обшибку */
        if (scenario.getName()==null){
            throwException("Необходимо задать имя сценария");
        }

        /* Если мы создаем сценарий с типом по умолчанию, то ошибка!*/
        if (scenario.isDefault()){
            throwException("Новый сценарий нельзя сделать сценарием по умолчанию.");
        }
        scenario.setKey(null);

        InfobipMessageGateway ims = null;
        InfobipObjects.Scenario infobipScenario = null;
        try {
            ims = new InfobipMessageGateway();
            ContentResponse response = ims.sendMessage(HttpMethod.POST, Config.getSettings().getInfobip().getScenariosUrl(), getInfobipScenario(scenario));
            ims.stop();
            infobipScenario = gson.fromJson(response.getContentAsString(), InfobipObjects.Scenario.class);
        } catch (Exception e) {
            if (ims!=null) ims.stop();
            throwException(e.getMessage());
        }

        if (infobipScenario==null){
            throwException("Ошибка создания сценария на сервере Infobip");
        }

        // Заводим сценарий в БД
        String newScenarioKey = infobipScenario.getKey();
        if (newScenarioKey==null){
            // Если ключ пустой, значит или ошибка запроса или формат данных поменялся
            if (infobipScenario.getServiceErrorMessage()!=null){
                // Ошибка сервиса
                throwException(infobipScenario.getServiceErrorMessage());
            } else {
                throwException("Ключ сценария отсутствует");
            }
        }

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        session.getTransaction().begin();

        //Сохраняем данные сценария в БД
        ScenarioDAO scDao = new ScenarioDAO(session);
        ScenarioChannelDAO scchDao = new ScenarioChannelDAO(session);

        String flow = gson.toJson(scenario.getFlow());

        try {
            Long scenarioId = scDao.addScenario(
                    newScenarioKey,
                    infobipScenario.getName(),
                    flow,
                    scenario.isActive(),
                    Config.getSettings().getInfobip().getLogin()
            );

            session.getTransaction().commit();
            scenario.setId(scenarioId);
            scenario.setKey(newScenarioKey);

        } catch (JDBCException e) {
            session.getTransaction().rollback();
            throwException("Ошибка создания сценария в БД: "+e.getSQLException().getMessage());
        }

        // Закрываем сессию
        session.close();

        // возвращаем в ответ данные созданного сценария
        return new JsonObject(scenario);
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonObject getScenario(@QueryParam("scenariokey") String scenarioKey) {

        authorizeOrThrow(adminRoles);

        JsonObject json;
        InfobipObjects.Scenario infobipScenario = null;
        Gson gson = Config.getGsonBuilder().create();
        InfobipMessageGateway ims = null;
        try {
            ims = new InfobipMessageGateway();
            String getUrl = Config.getSettings().getInfobip().getScenariosUrl()+"/"+scenarioKey;
            ContentResponse response = ims.sendMessage(HttpMethod.GET, getUrl, null);
            ims.stop();
            infobipScenario = gson.fromJson(response.getContentAsString(), InfobipObjects.Scenario.class);

        } catch (Exception e) {
            if (ims!=null) ims.stop();
            throwException(e.getMessage());
        }

        if (infobipScenario==null) throwException("Ошибка запроса сценария на сервере Infobip");

        // Если сервис вернул ошибку, показываем её
        if (infobipScenario.getServiceErrorMessage()!=null){
            // Ошибка сервиса
            throwException(infobipScenario.getServiceErrorMessage());
        }

        // Обновляем данные в БД
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

        ScenarioDAO scDao = new ScenarioDAO(session);
        ScenarioChannelDAO scchDao = new ScenarioChannelDAO(session);
        com.bankir.mgs.hibernate.model.Scenario hibernateScenario;

        ScenarioObject scenario = null;
        // Открываем сессию с транзакцией
        session.getTransaction().begin();
        //Сохраняем данные сценария в БД
        try {
            hibernateScenario = scDao.getByKey(scenarioKey, Config.getSettings().getInfobip().getLogin());

            hibernateScenario.setScenarioName(infobipScenario.getName());
            List<ScenarioObject.Flow> listFlow = new ArrayList<>();

            for (InfobipObjects.Flow infobipFlow:infobipScenario.getFlow()){
                listFlow.add(new ScenarioObject.Flow(infobipFlow.getChannel(), infobipFlow.getFrom()));
            }

            String flow = gson.toJson(listFlow);
            hibernateScenario.setFlow(flow);

            scDao.save(hibernateScenario);
            session.getTransaction().commit();
            scenario = getScenario(hibernateScenario);

        } catch (JDBCException e) {
            session.getTransaction().rollback();
            throwException("Ошибка сохранения сценария в БД: " + e.getSQLException().getMessage());
        }
        session.close();

        return new JsonObject(scenario);
    }


    @PUT
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public JsonObject update(ScenarioObject scenario, @PathParam("id") Long scenarioId) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        String scenarioKey = scenario.getKey();

        /* Если имя сценария не задали, то выводим обшибку */
        if (scenarioKey==null){

            throwException("Необходимо задать ключ сценария");
        }

        /* Если имя сценария не задали, то выводим обшибку */
        if (scenario.getName()==null){
            throwException("Необходимо задать имя сценария");
        }

        /* Если имя сценария не задали, то выводим обшибку */
        if (scenario.getName()==null){
            throwException("Необходимо задать имя сценария");
        }

        JsonObject json = null;

        // Обновляем данные сценария в Инфобипе
        InfobipMessageGateway ims = null;
        InfobipObjects.Scenario infobipScenario = null;
        try {
            ims = new InfobipMessageGateway();
            String putUrl = Config.getSettings().getInfobip().getScenariosUrl()+"//"+scenarioKey;
            ContentResponse response = ims.sendMessage(HttpMethod.PUT, putUrl, getInfobipScenario(scenario));
            ims.stop();
            infobipScenario = gson.fromJson(response.getContentAsString(), InfobipObjects.Scenario.class);
        } catch (Exception e) {
            if (ims!=null) ims.stop();
            throwException(e.getMessage());
        }

        if (infobipScenario.getServiceErrorMessage()!=null) {
            // Ошибка сервиса
            throwException(infobipScenario.getServiceErrorMessage());
        }


        // Обновляем данные в БД
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

        ScenarioDAO scDao = new ScenarioDAO(session);
        ScenarioChannelDAO scchDao = new ScenarioChannelDAO(session);
        com.bankir.mgs.hibernate.model.Scenario scenarioInBD;

        // Открываем сессию с транзакцией
        session.getTransaction().begin();
        //Сохраняем данные сценария в БД
        try {
            scenarioInBD = scDao.getById(scenarioId);
            //Обновляем сведения о имени сценария в объекте
            scenarioInBD.setScenarioName(scenario.getName());
            String flow = gson.toJson(scenario.getFlow());
            scenarioInBD.setFlow(flow);
            scenarioInBD.setActive(scenario.isActive());
            scenarioInBD.setInfobipLogin(Config.getSettings().getInfobip().getLogin());

            scDao.save(scenarioInBD);
            session.getTransaction().commit();


        } catch (JDBCException e) {
            session.getTransaction().rollback();
            throwException("Ошибка сохранения сценария в БД: " + e.getSQLException().getMessage());
        }
        session.close();

        return new JsonObject(true);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public JsonObject list(
            @QueryParam("start") int start,
            @QueryParam("limit") int limit,
            @QueryParam("page") int page,
            @QueryParam("sort") String sort,
            @QueryParam("filter") String filter,
            @DefaultValue("Y") @QueryParam("active") String active
    ){
        /* Авторизация пользователя по роли */
        authorizeOrThrow(viewScenariosRoles);

        try {
            StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

            List<FilterProperty> filterProperties = Utils.parseFilterProperties(filter);
            List<SorterProperty> sorterProperties = Utils.parseSortProperties(sort);

            String hqlFrom = " FROM Scenario s ";
            String hqlWhere = " WHERE infobipLogin=:login ";

            if ("Y".equalsIgnoreCase(active)){
                hqlWhere += " and s.active=true";
            }

            filterProperties.add(new FilterProperty("login",Config.getSettings().getInfobip().getLogin()));

            Query countQuery = Utils.createQuery(session, "Select count (s.scenarioKey) "+hqlFrom+hqlWhere, null, null, filterProperties, null);
            Long countResults = (Long) countQuery.uniqueResult();

            Query query = Utils.createQuery(session, hqlFrom+hqlWhere, start, limit,filterProperties, sorterProperties)
                    .setReadOnly(true);

            List<ScenarioObject> scenarios = new ArrayList<>();
            List hibernateScenarios =  query.list();
            for (Object hibernateScenario:hibernateScenarios){
                scenarios.add(getScenario((com.bankir.mgs.hibernate.model.Scenario) hibernateScenario));
            }
            JsonObject json = new JsonObject(scenarios);
            json.setTotal(countResults);

            session.close();

            return json;
        }catch(JDBCException e){
            return new JsonObject(e.getSQLException().getMessage());
        }
    }


    private String getInfobipScenario(ScenarioObject scenario){

        InfobipObjects.Scenario infobipScenario = new InfobipObjects.Scenario(
                scenario.getKey(),
                scenario.getName(),
                Config.getSettings().getDefaultScenarioKey().equalsIgnoreCase(scenario.getKey())
        );

        List<InfobipObjects.Flow> listInfobipFlow = new ArrayList<>();

        for (ScenarioObject.Flow flow:scenario.getFlow()){
            listInfobipFlow.add(new InfobipObjects.Flow(flow.getFrom(), flow.getChannel()));
        }
        infobipScenario.setFlow(listInfobipFlow);
        return gson.toJson(infobipScenario);
    }


    private ScenarioObject getScenario(com.bankir.mgs.hibernate.model.Scenario hibernateScenario){
        ScenarioObject scenario = new ScenarioObject(
                hibernateScenario.getId(),
                hibernateScenario.getScenarioName(),
                hibernateScenario.getScenarioKey(),
                hibernateScenario.isActive(),
                (hibernateScenario.getScenarioKey().equalsIgnoreCase(Config.getSettings().getDefaultScenarioKey()))
        );
        Type listType = new TypeToken<ArrayList<ScenarioObject.Flow>>(){}.getType();
        List<ScenarioObject.Flow> flows = gson.fromJson(hibernateScenario.getFlow(), listType);
        scenario.setFlow(flows);
        return scenario;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public JsonObject delete(ScenarioObject scenario, @PathParam("id") Long id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

        //Проверяем наличие сообщений с таким типом

        List<FilterProperty> filterProperties = new ArrayList<>();
        filterProperties.add(new FilterProperty("scenarioId", id));
        Query query = Utils.createQuery(session,  "From Message where scenarioId=:scenarioId", 0, 1, filterProperties, null)
                .setReadOnly(true);
        List result = query.list();

        if (result.size()>0){
            throwException("Удаление невозможно. Сценарий \""+scenario.getName()+"\" использован в сообщениях!");
        }

        session.getTransaction().begin();
        //Сохраняем данные сценария в БД
        ScenarioDAO scDao = new ScenarioDAO(session);
        try {
            com.bankir.mgs.hibernate.model.Scenario hibernateScenario = new com.bankir.mgs.hibernate.model.Scenario();
            hibernateScenario.setId(id);
            scDao.delete(hibernateScenario);
            session.getTransaction().commit();

        }catch (JDBCException e){
            session.getTransaction().rollback();
            throwException("Ошибка удаления сценария в БД: " + e.getSQLException());
        }

        // Закрываем сессию
        session.close();

        return successJsonObject;

    }
}