Ext.define('admins.model.UserMessageType', {
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		url: "/rest/usermessagetypes",
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
		actionMethods:{ create: 'POST', read: 'GET', destroy: 'DELETE' },
		writer:{type:'json', writeAllFields:true}
	},
    fields: [
		{name:'typeId', type:'string'},
		{name:'userId', type:'number'}		
    ]
});
