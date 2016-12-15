Ext.define('admins.model.UserRole', {
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		url: "/rest/userroles",
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
	idProperty: 'roleName',
    fields: [
		{name:'userId', type:'number'},
		{name:'roleName', type:'string'}
    ]
});
