Ext.define('opers.model.Report', {
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		url: "/rest/reports",
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
		}
	},
	idProperty: 'id',
    fields: [
		{name:'id', type:'auto'},
		{name:'phoneNumber', type:'string'},
		{name:'smsText', type:'string'},
		{name:'viberText', type:'string'},
		{name:'sentAt', type:'date', dateFormat:'Y-m-d H:i:s'},
		{name:'doneAt', type:'date', dateFormat:'Y-m-d H:i:s'},
		{name:'reportDate', type:'date', dateFormat:'Y-m-d H:i:s'},		
		{name:'channel', type:'string'},
		{name:'messageCount', type:'number'},
		{name:'pricePerMessage', type:'number'},
		{name:'priceCurrency', type:'string'},
		{name:'statusName', type:'string'},
		{name:'statusGroup', type:'string'},
		{name:'statusDescription', type:'string'}
    ]
});
