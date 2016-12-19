Ext.define('opers.controller.SendOneToAll', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.SendOneToAllController',
	init: function(){
		var me = this,
			dataPanel = me.lookupReference('dataPanel'),
			mtStore = Ext.data.StoreManager.lookup('messageTypesStore'),
			messageType = mtStore.getById('MESSAGE');
		
		//dataPanel.reset();
		//dataPanel.gridPhones.getStore().removeAll();
		me.lookupReference('description').disable();

		var toSms = me.lookupReference('toSms'),
			toViber = me.lookupReference('toViber'),
			toVoice = me.lookupReference('toVoice'),
			toParseco = me.lookupReference('toParseco');
			
		(messageType.data.acceptSms?toSms.enable():toSms.disable());
		(messageType.data.acceptViber?toViber.enable():toViber.disable());
		(messageType.data.acceptVoice?toVoice.enable():toVoice.disable());
		(messageType.data.acceptParseco?toParseco.enable():toParseco.disable());
	},
	// Renderers
	
	//Events
	
	//Buttons
	btnAddPhone: function(){
		var me = this;
		var app = me.app;
		var dataPanel = me.lookupReference('dataPanel');
		var fieldValues = dataPanel.getForm().getFieldValues();	
		if (fieldValues.phones.length>=11){
			
			var validPhones = [],
				invalidPhones = [],
				phoneRe = /^([0-9]{11})$/,
				phones = fieldValues.phones.split('\n');
			
			phones.forEach( function(phone, index, array){
				if (phone!=""){
					if (phoneRe.test(phone)){
						validPhones.push(phone);
					} else{
						invalidPhones.push(phone);
					}
				}
			});
			
			var store = me.lookupReference('gridPhones').getStore();
			validPhones.forEach(function(phone, index, array){
				if (store.find("phone", phone)==-1) store.add({phone:phone});
			});
			
			var restPhones = "";
			invalidPhones.forEach(function(phone, index, array){
				restPhones+=phone+"\n";
			});
			
			dataPanel.getForm().setValues({phones:restPhones});
			
			var descriptionField = me.lookupReference('description');
			(store.getCount()>1?descriptionField.enable():descriptionField.disable());
		}
	},
	btnStart: function(){
		var me = this,
			dataPanel = me.lookupReference('dataPanel');
		
		if (!dataPanel.isValid()) return;
		
		var store = me.lookupReference('gridPhones').getStore();
	
		if (store.getCount()==0){
			Ext.Msg.alert('Ошибка', 'Добавьте получателей');
			return;
		}
				
		var fieldValues = dataPanel.getForm().getFieldValues();
		var message = {};
		var requestData = {messages:[]},
			toSms = (fieldValues.toSms==1),
			toViber = (fieldValues.toViber==1),
			toVoice = (fieldValues.toVoice==1),
			toParseco = (fieldValues.toParseco==1);
		
		if (store.getCount()>1) Ext.apply(requestData, {description:fieldValues.description});		
		
		store.each(function(rec) {
			
			message = {
				text:fieldValues.text,
				scenarioKey:fieldValues.scenario,
				toSms: toSms,
				toViber: toViber,
				toVoice: toVoice,
				toParseco: toParseco,
				phoneNumber: rec.get('phone'),
				messageId: rec.get('phone')				
			}			
			requestData.messages.push(message);
		});
		
		/*
		for (var i=1;i<1000;i++){
			message.phoneNumber = 10000000000 + i;
			message.messageId = message.phone;
			requestData.messages.push(message);
		}
		 */
		
		console.log(requestData);
		
		me.getView().getEl().mask({msg:"Создание рассылки"});
		
		Ext.Ajax.request({
			url: '/rest/messages/create',
			method:'POST',
			timeout:0,
			jsonData: requestData,
			success: function(response){				
				var jResponse = Ext.decode(response.responseText);
				console.log(jResponse);
				if (jResponse.success){					
					//Редирект на репорт или сообщение об успехе? Нужно обработать ответ и выдать сколько успешно, а сколько не успешно.
					Ext.Msg.alert('Формирование рассылки сообщений', 'Успешно сформировано - ' + jResponse.successMessages.length+'<br/>Ошибочных - ' + jResponse.failedMessages.length);
					Ext.Array.each(jResponse.successMessages, function(item){
						var rec = store.findRecord('phone', item.messageId);
						store.remove(rec);
						store.commitChanges();
					});
				} else {
					Ext.Msg.alert('Ошибка', jResponse.message);
				}
				me.getView().getEl().unmask();
			},
			failure: function(a,b,c){
				me.getView().getEl().unmask();
			}
		});	
	}	
});
