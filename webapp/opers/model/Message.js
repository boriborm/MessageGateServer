Ext.define('opers.model.Message', {
    extend: 'Ext.data.Model',
	proxy: {
//		type: 'jwg_proxy',
		type: 'rest',
		url: "/rest/messages",
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

		/*
		writer:{type:'json', rootProperty:'data', writeAllFields:true},
		api:{
			create:'/json/session@jwg_comissions.addClient',
			destroy:'/json/session@jwg_comissions.removeClient'
		}
		* */
	},
	idProperty: 'id',
    fields: [
		{name:'id', type:'auto'},
		{name:'phoneNumber', type:'string'},
		{name:'smsText', type:'string'},
		{name:'viberText', type:'string'},
		{name:'createDate', type:'date', dateFormat:'Y-m-d H:i:s'},
		{name:'user', reference: 'MessageUser'},
		{name:'scenario', reference: 'Scenario'},
		{name:'messageType', reference: 'MessageType'}
    ]
});

Ext.define('opers.model.MessageUser', {
    extend: 'Ext.data.Model',
    idProperty: 'id',
    fields: ['id', 'userName']
});
