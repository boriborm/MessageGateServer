package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.SorterProperty;
import com.bankir.mgs.User;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.hibernate.dao.ScenarioDAO;
import com.bankir.mgs.infobip.InfobipMessageGateway;
import com.bankir.mgs.infobip.model.InfobipObjects;
import com.bankir.mgs.jersey.model.MgsJsonObject;
import com.bankir.mgs.jersey.model.ScenarioObject;
import com.google.gson.Gson;
import org.eclipse.jetty.http.HttpMethod;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/scenarios")
public class Scenarios extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(Scenarios.class);
    private String[] viewScenariosRoles = {User.ROLE_ADMIN, User.ROLE_READER, User.ROLE_EDITOR};
    private Gson gson = Config.getGsonBuilder().create();

    @POST
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public MgsJsonObject create(ScenarioObject scenario) {

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
            com.google.gson.JsonObject response = ims.sendMessage(HttpMethod.POST, Config.getSettings().getInfobip().getScenariosUrl(), getInfobipScenario(scenario));
            infobipScenario = gson.fromJson(response, InfobipObjects.Scenario.class);
        } catch (Exception e) {
            logger.error("Error: "+e.getMessage(), e);
            throwException(e.getMessage());
        } finally {
            if (ims!=null) try {ims.stop();} catch (Exception ignored){}
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
        StatelessSession session = null;
        MgsJsonObject result;
        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();
            session.getTransaction().begin();
            //Сохраняем данные сценария в БД
            ScenarioDAO scDao = new ScenarioDAO(session);

            Long scenarioId = scDao.addScenario(
                    newScenarioKey,
                    infobipScenario.getName(),
                    scenario.getFlow(),
                    scenario.isActive(),
                    Config.getSettings().getInfobip().getLogin()
            );

            session.getTransaction().commit();
            scenario.setId(scenarioId);
            scenario.setKey(newScenarioKey);
            result = new MgsJsonObject(scenario);
        } catch (JDBCException e) {
            logger.error("Error: " + e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){};
            result = new MgsJsonObject("Ошибка создания сценария в БД: "+e.getSQLException().getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){};
        }

        // возвращаем в ответ данные созданного сценария
        return result;
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public MgsJsonObject getScenario(@QueryParam("scenariokey") String scenarioKey) {

        authorizeOrThrow(adminRoles);

        InfobipObjects.Scenario infobipScenario = null;
        Gson gson = Config.getGsonBuilder().create();
        InfobipMessageGateway ims = null;
        try {
            ims = new InfobipMessageGateway();
            String getUrl = Config.getSettings().getInfobip().getScenariosUrl()+"/"+scenarioKey;
            com.google.gson.JsonObject response = ims.sendMessage(HttpMethod.GET, getUrl, null);
            infobipScenario = gson.fromJson(response, InfobipObjects.Scenario.class);
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage(), e);
            throwException(e.getMessage());
        } finally {
            if (ims!=null) try {ims.stop();} catch (Exception ignored){}
        }

        if (infobipScenario==null) throwException("Ошибка запроса сценария на сервере Infobip");

        // Если сервис вернул ошибку, показываем её
        if (infobipScenario.getServiceErrorMessage()!=null){
            // Ошибка сервиса
            throwException(infobipScenario.getServiceErrorMessage());
        }

        // Обновляем данные в БД
        StatelessSession session = null;
        ScenarioObject scenario = null;
        MgsJsonObject result;
        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();
            ScenarioDAO scDao = new ScenarioDAO(session);

            // Открываем сессию с транзакцией
            session.getTransaction().begin();
            //Сохраняем данные сценария в БД

            com.bankir.mgs.hibernate.model.Scenario hibernateScenario = scDao.getByKey(scenarioKey, Config.getSettings().getInfobip().getLogin());

            hibernateScenario.setScenarioName(infobipScenario.getName());
            List<ScenarioObject.Flow> listFlow = new ArrayList<>();

            for (InfobipObjects.Flow infobipFlow:infobipScenario.getFlow()){
                listFlow.add(new ScenarioObject.Flow(infobipFlow.getChannel(), infobipFlow.getFrom()));
            }

            hibernateScenario.setFlow(listFlow);
            scDao.save(hibernateScenario);
            session.getTransaction().commit();
            scenario = getScenario(hibernateScenario);
            result = new MgsJsonObject(scenario);

        } catch (JDBCException e) {
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){}
            result = new MgsJsonObject("Ошибка сохранения сценария в БД: " + e.getSQLException().getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){};
        }

        return result;
    }


    @PUT
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public MgsJsonObject update(ScenarioObject scenario, @PathParam("id") Long scenarioId) {

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



        // Обновляем данные сценария в Инфобипе
        InfobipMessageGateway ims = null;
        InfobipObjects.Scenario infobipScenario = null;
        try {
            ims = new InfobipMessageGateway();
            String putUrl = Config.getSettings().getInfobip().getScenariosUrl()+"//"+scenarioKey;
            com.google.gson.JsonObject response = ims.sendMessage(HttpMethod.PUT, putUrl, getInfobipScenario(scenario));
            infobipScenario = gson.fromJson(response, InfobipObjects.Scenario.class);
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage(), e);
            throwException(e.getMessage());
        } finally {
            if (ims!=null) try {ims.stop();} catch (Exception ignored){}
        }

        if (infobipScenario.getServiceErrorMessage()!=null) {
            // Ошибка сервиса
            throwException(infobipScenario.getServiceErrorMessage());
        }

        MgsJsonObject result = null;

        // Обновляем данные в БД
        StatelessSession session = null;
        //Сохраняем данные сценария в БД
        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();
            ScenarioDAO scDao = new ScenarioDAO(session);
            com.bankir.mgs.hibernate.model.Scenario scenarioInBD;
            // Открываем сессию с транзакцией
            session.getTransaction().begin();

            scenarioInBD = scDao.getById(scenarioId);
            //Обновляем сведения о имени сценария в объекте
            scenarioInBD.setScenarioName(scenario.getName());
            scenarioInBD.setFlow(scenario.getFlow());
            scenarioInBD.setActive(scenario.isActive());
            scenarioInBD.setInfobipLogin(Config.getSettings().getInfobip().getLogin());

            scDao.save(scenarioInBD);
            session.getTransaction().commit();

            result = MgsJsonObject.Success();

        } catch (JDBCException e) {
            logger.error("Error: " + e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){};
            result = new MgsJsonObject("Ошибка сохранения сценария в БД: " + e.getSQLException().getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){};
        }

        return result;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public MgsJsonObject list(
            @QueryParam("start") int start,
            @QueryParam("limit") int limit,
            @QueryParam("page") int page,
            @QueryParam("sort") String sort,
            @QueryParam("filter") String filter,
            @DefaultValue("Y") @QueryParam("active") String active
    ){
        /* Авторизация пользователя по роли */
        authorizeOrThrow(viewScenariosRoles);
        StatelessSession session = null;
        MgsJsonObject result;
        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();

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
            MgsJsonObject json = new MgsJsonObject(scenarios);
            json.setTotal(countResults);

            result = json;
        }catch(JDBCException e){
            logger.error("Error: " + e.getSQLException().getMessage(), e);
            result = new MgsJsonObject(e.getSQLException().getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){};
        }
        return result;
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
        scenario.setFlow(hibernateScenario.getFlow());
        return scenario;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public MgsJsonObject delete(ScenarioObject scenario, @PathParam("id") Long id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);
        if (scenario.getKey().equals(Config.getSettings().getDefaultScenarioKey())){
            throwException("Удаление сценария по умолчанию невозможно!");
        }
        MgsJsonObject result;
        // Открываем сессию с транзакцией
        StatelessSession session = null;
        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();
            //Проверяем наличие сообщений с таким типом

            List<FilterProperty> filterProperties = new ArrayList<>();
            filterProperties.add(new FilterProperty("scenarioId", id));
            Query query = Utils.createQuery(session,  "From Message where scenarioId=:scenarioId", 0, 1, filterProperties, null)
                    .setReadOnly(true);
            List msg = query.list();

            if (msg.size()>0){
                result = new MgsJsonObject("Удаление невозможно. Сценарий \""+scenario.getName()+"\" использован в сообщениях!");
            } else {
                session.getTransaction().begin();
                //Сохраняем данные сценария в БД
                ScenarioDAO scDao = new ScenarioDAO(session);
                com.bankir.mgs.hibernate.model.Scenario hibernateScenario = new com.bankir.mgs.hibernate.model.Scenario();
                hibernateScenario.setId(id);
                scDao.delete(hibernateScenario);
                session.getTransaction().commit();
                return MgsJsonObject.Success();
            }
        }catch (JDBCException e){
            logger.error("Error: " + e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){};
            result = new MgsJsonObject("Ошибка удаления сценария в БД: " + e.getSQLException().getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){};
        }

        return result;

    }
}