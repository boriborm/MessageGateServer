Ext.define('opers.model.Scenario', {
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		url: "/rest/scenarios",
		reader:{type:'json', rootProperty:'data' /*, totalProperty:'results', messageProperty:'error.reason'*/},
		listeners : {
			exception : function(proxy, response, operation) {
				try {
					var jResponse = Ext.decode(response.responseText);
					Ext.Msg.alert('Ошибка', jResponse.message);
				} catch (err){
					Ext.Msg.alert('Ошибка', response.statusText);
				}
			}
		}
	},
	idProperty: 'id',
    fields: [
		{name:'id', type:'auto'},
		{name:'scenarioName', type:'string'},
		{name:'scenarioKey', type:'string'},
		{name:'active', type:'bool'},
    ]
});
