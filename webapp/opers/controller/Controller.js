Ext.define('opers.controller.Controller', {
	extend: 'Ext.app.Controller',

	views: ['Messages','SendOneToAll','PhoneGrants'],
	stores:['Messages', 'MessageTypes', 'Scenarios', 'Reports','PhoneGrants','Bulks'],
	models:['Message','MessageType', 'Scenario', 'Report','PhoneGrant','Bulk'],

	init: function(){
		var me = this;
		
		me.control({
			'#btnToolbarMessages': {
            	click: me.openMessagesPanel
	        },
	        '#btnToolbarSendOneToAll': {
            	click: me.openSendOneToAll
	        },
	        '#btnToolbarPhoneGrants': {
            	click: me.openPhoneGrants
	        },
	        '#messagesPanel #gridPanel': {
				select: me.messageSelect
			},
			'#messagesPanel #btnMessagesFilter':{
				click:me.msgFilter
			},
			'#messagesPanel #btnMessagesFilterClear':{
				click:me.msgFilterClear
			},
			'#sendOneToAllPanel #btnAddPhone': {
            	click: me.sotaAddPhone
	        },
			'#sendOneToAllPanel #btnStart': {
            	click: me.sotaStart
	        },   
	        '#phoneGrantsPanel #btnSavePhoneGrants': {
            	click: me.pgSave
	        },
	        '#phoneGrantsPanel #phoneNumber': {
            	select: me.pgSelect
	        }, 
	        '#phoneGrantsPanel checkboxfield': {
            	change: me.pgChange
	        }, 
		});
	},
	
	openMessagesPanel: function(){	
		var me = this;
		me.messagesPanel.gridPanel.getStore().load();
		me.app.setDataPanel (me.messagesPanel);
		me.msgFilter();
	},
	
	openSendOneToAll: function(){	
		var me = this,
			dataPanel = me.sendOneToAllPanel.dataPanel,
			mtStore = Ext.data.StoreManager.lookup('messageTypesStore'),
			messageType = mtStore.getById('MESSAGE');
		
		dataPanel.reset();
		dataPanel.gridPhones.getStore().removeAll();
		dataPanel.query('#description')[0].disable();

		var toSms = dataPanel.query('#toSms')[0],
			toViber = dataPanel.query('#toViber')[0],
			toVoice = dataPanel.query('#toVoice')[0],
			toParseco = dataPanel.query('#toParseco')[0];
			
		(messageType.data.acceptSms?toSms.enable():toSms.disable());
		(messageType.data.acceptViber?toViber.enable():toViber.disable());
		(messageType.data.acceptVoice?toVoice.enable():toVoice.disable());
		(messageType.data.acceptParseco?toParseco.enable():toParseco.disable());
		
		me.app.setDataPanel (me.sendOneToAllPanel);
	},
	
	openPhoneGrants: function(){	
		var me = this;		
		me.phoneGrantsPanel.dataPanel.reset();
		me.phoneGrantsPanel.dataPanel.query('#btnSavePhoneGrants')[0].disable();
		me.app.setDataPanel (me.phoneGrantsPanel);		
	},
	
	msgFilter: function(){
		var me = this;
		var app = me.app;
		if (me.messagesPanel.filterPanel.isValid()){
			var fieldValues = me.messagesPanel.filterPanel.getForm().getFieldValues();
			var store = me.messagesPanel.gridPanel.getStore();
			var filters = [];
			if (fieldValues.beginDate) filters.push({property:"beginDate", value:fieldValues.beginDate});
			if (fieldValues.endDate) filters.push({property:"endDate", value:fieldValues.endDate});
			if (fieldValues.messageType) filters.push({property:"typeId", value:fieldValues.messageType});
			if (fieldValues.bulkId) filters.push({property:"bulkId", value:fieldValues.bulkId});
			if (fieldValues.phone) filters.push({property:"phoneNumber", value:fieldValues.phone});
			
			store.clearFilter();
			if (filters.length>0){
							
				store.filter(filters);
			} else store.load();
			//store.load({params:fieldValues});
			
		}
	},
	msgFilterClear: function(){
		var me = this;
		var app = me.app;
		me.messagesPanel.filterPanel.reset();
	},
	messageSelect: function(grid, record){
		var me = this;
		var app = me.app;
		var dataPanel = me.messagesPanel.dataPanel;
		dataPanel.expand();
		console.log(record);
		console.log(record.get('createDate'));
		var data = { 
			id: record.getId(),
			externalId: record.get('externalId'),
			smsText: record.get('smsText'),
			viberText: record.get('viberText'),
			voiceText: record.get('voiceText'),
			parsecoText: record.get('parsecoText'),
			user: record.data.user.userName,
			createDate: record.get('createDate'),
			scenario: record.data.scenario.scenarioName
		};
		dataPanel.currentRecord = record;
		dataPanel.getForm().setValues(data);
		dataPanel.reportsGrid.getStore().load({params:{messageId:data.id}});
		
	},
	sotaAddPhone: function(){
		var me = this;
		var app = me.app;
		var dataPanel = me.sendOneToAllPanel.dataPanel;
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
			
			var store = dataPanel.gridPhones.getStore();
			validPhones.forEach(function(phone, index, array){
				if (store.find("phone", phone)==-1) store.add({phone:phone});
			});
			
			var restPhones = "";
			invalidPhones.forEach(function(phone, index, array){
				restPhones+=phone+"\n";
			});
			
			dataPanel.getForm().setValues({phones:restPhones});
			
			var descriptionField = dataPanel.query('#description')[0];
			(store.getCount()>1?descriptionField.enable():descriptionField.disable());
		}
	},
	sotaStart: function(){
		var me = this;
		var app = me.app;
		var dataPanel = me.sendOneToAllPanel.dataPanel;
		
		if (!dataPanel.isValid()) return;
		
		var store = dataPanel.gridPhones.getStore();
	
		if (store.getCount()==0) return;
				
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
		
		app.globalViewPort.getEl().mask({msg:"Создание рассылки"});
		
		Ext.Ajax.request({
			url: '/rest/messages/create',
			method:'POST',
			timeout:0,
			jsonData: requestData,
			success: function(response){				
				var jResponse = Ext.decode(response.responseText);
				if (jResponse.success){					
					//Редирект на репорт или сообщение об успехе? Нужно обработать ответ и выдать сколько успешно, а сколько не успешно.
				} else {
					Ext.Msg.alert('Ошибка', jResponse.message);					
				}
				app.globalViewPort.getEl().unmask();
			},
			failure: function(a,b,c){
				console.log(a);
				console.log(b);
				console.log(c);
				app.globalViewPort.getEl().unmask();
			}
		});	
	},
	pgSave: function(){
		var me = this;
		var app = me.app;
		var dataPanel = me.phoneGrantsPanel.dataPanel;
		
		if (!dataPanel.isValid()) return;
		
		var fieldValues = dataPanel.getForm().getFieldValues();
		
		var requestData = {
			phoneNumber: fieldValues.phoneNumber,
			acceptSms: (fieldValues.acceptSms==1),
			acceptViber: (fieldValues.acceptViber==1),
			acceptVoice: (fieldValues.acceptVoice==1),
			acceptParseco: (fieldValues.acceptParseco==1)
		};		

		app.globalViewPort.getEl().mask({msg:"Сохранение информации"});
		
		Ext.Ajax.request({
			url: '/rest/phonegrants/create',
			method:'POST',
			jsonData: requestData,
			success: function(response){
				var jResponse = Ext.decode(response.responseText);
					Ext.Msg.alert('Сообщение', 'Информация сохранена.');
					dataPanel.query('#btnSavePhoneGrants')[0].disable();
				if (!jResponse.success){
					Ext.Msg.alert('Ошибка', jResponse.message);
				};
				app.globalViewPort.getEl().unmask();
			},
			failure: function(){
				app.globalViewPort.getEl().unmask();
			}
		});	
	},
	
	pgSelect: function(){
		var me = this,
			app = me.app,
			dataPanel = me.phoneGrantsPanel.dataPanel,
			phoneNumberField = dataPanel.query('#phoneNumber')[0],
			store = phoneNumberField.getStore(),
			rec = store.getById(phoneNumberField.getValue());				
			
		dataPanel.getForm().setValues(rec.data);
		dataPanel.query('#btnSavePhoneGrants')[0].disable();
	},
	pgChange: function(){
		var me = this;
		var app = me.app;
		var dataPanel = me.phoneGrantsPanel.dataPanel;
		
		dataPanel.query('#btnSavePhoneGrants')[0].enable();
	}
	
});
