Ext.define('opers.controller.PhoneGrants', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.PhoneGrantsController',
	init: function(){
		this.lookupReference('btnSave').disable();
		this.lookupReference('dataPanel').reset();
	},
	// Renderers
	
	//Events
	
	change: function(){
		this.lookupReference('btnSave').enable();		
	},

	select: function(){
		var me = this,
			dataPanel = me.lookupReference('dataPanel'),
			phoneNumberField = me.lookupReference('phoneNumber'),
			store = phoneNumberField.getStore(),
			rec = store.getById(phoneNumberField.getValue());				
			
		dataPanel.getForm().setValues(rec.data);
		me.lookupReference('btnSave').disable();
	},
	
	//Buttons	
	
	btnSave: function(){
		var me = this;
			dataPanel = me.lookupReference('dataPanel');
		
		if (!dataPanel.isValid()) return;
		
		var fieldValues = dataPanel.getForm().getFieldValues();
		
		var requestData = {
			phoneNumber: fieldValues.phoneNumber,
			acceptSms: (fieldValues.acceptSms==1),
			acceptViber: (fieldValues.acceptViber==1),
			acceptVoice: (fieldValues.acceptVoice==1),
			acceptParseco: (fieldValues.acceptParseco==1)
		};		

		me.getView().getEl().mask({msg:"Сохранение информации"});
		
		Ext.Ajax.request({
			url: '/rest/phonegrants/create',
			method:'POST',
			jsonData: requestData,
			success: function(response){
				
				var jResponse = Ext.decode(response.responseText);
				
				if (jResponse.success){
					Ext.Msg.alert('Сообщение', 'Информация сохранена.');
				}else{
					Ext.Msg.alert('Ошибка', jResponse.message);
				};
				
				me.lookupReference('btnSave').disable();
				me.getView().getEl().unmask();
			},
			failure: function(){
				me.getView().getEl().unmask();
			}
		});	
	}
	
		
});
