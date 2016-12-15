Ext.define('admins.model.User', {
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		url: "/rest/users",
		reader:{type:'json', rootProperty:'data'},
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
		{name:'login', type:'string'},
		{name:'password', type:'string'},
		{name:'userName', type:'string'},
		{name:'locked', type:'bool'},
		{name:'roles', type:'auto'}
    ]
});
