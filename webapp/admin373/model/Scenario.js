Ext.define('admins.model.Scenario', {
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		url: "/rest/scenarios",
		reader:{type:'json', rootProperty:'data'},
		extraParams:{active:'ALL'},
		listeners : {
			exception : function(proxy, response, operation) {
				try {
					var jResponse = Ext.decode(response.responseText);
					Ext.Msg.alert('Ошибка', jResponse.message);
				} catch (err){
					Ext.Msg.alert('Ошибка', response.statusText);
				}
			}
		},
		actionMethods:{ create: 'POST', read: 'GET', update: 'PUT', destroy: 'DELETE' },
		writer:{type:'json', writeAllFields:true}
	},
	idProperty: 'id',
    fields: [
		{name:'id', type:'number'},
		{name:'name', type:'string'},
		{name:'key', type:'string'},
		{name:'active', type:'bool'},
		{name:'isDefault', type:'bool', persis:true},
		{name:'flow', reference :'ScenarioFlow'}		
    ]
});

Ext.define('admins.model.ScenarioFlow', {
    extend: 'Ext.data.Model',
    idProperty: 'channel',
    fields: [
		{name: '', type:'auto'},
		'channel', 
		'from'
	]
});
