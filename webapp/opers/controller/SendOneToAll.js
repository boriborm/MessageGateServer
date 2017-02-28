Ext.define('opers.controller.SendOneToAll', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.SendOneToAllController',
	init: function(){
		var me = this,
			dataPanel = me.lookupReference('dataPanel'),
			mtStore = Ext.data.StoreManager.lookup('messageTypesStore'),
			messageType = mtStore.getById('MESSAGE');
		
		me.lookupReference('description').disable();
	},
	// Renderers
	channelRenderer:function(channel){
		switch (channel) {		
			case 'S':
				return 'SMS';
			case 'V':
				return 'VIBER';
			case 'P':
				return 'PARSECO';
			case 'O':
				return 'VOICE';
			case 'F':
				return 'FACEBOOK';
		};
		return channel;
	},	
	//Events
	
	
	navigate: function(layout, direction){
		var me = this;
		// This routine could contain business logic required to manage the navigation steps.
		// It would call setActiveItem as needed, manage navigation button state, handle any
		// branching logic that might be required, handle alternate actions like cancellation
		// or finalization, etc.  A complete wizard implementation could get pretty
		// sophisticated depending on the complexity required, and should probably be
		// done as a subclass of CardLayout in a real-world implementation.
		layout[direction]();
		me.lookupReference('btnMovePrev').setDisabled(!layout.getPrev());
		me.lookupReference('btnMoveNext').setDisabled(!layout.getNext());
	},
	
	//Buttons
	
	goBack: function(btn){
		var layout = this.lookupReference('dataPanel').getLayout();
		var itemId = layout.getActiveItem().getId();
		console.log(itemId);
		var store = this.lookupReference('gridPhones').getStore();
		if (itemId == 'step-5'){			
			if (store.getCount()<=1){
				layout.setActiveItem('step-4');
			};			 
		};
		
		this.navigate(layout, "prev");
	},
	goNext: function(btn){
		var layout = this.lookupReference('dataPanel').getLayout();
		var itemId = layout.getActiveItem().getId();
		
		if (itemId == 'step-1'){
			var store = this.lookupReference('gridChannels').getStore();			
			if (store.getCount() == 0){
				Ext.Msg.alert('Ошибка', 'Добавьте канал');
				return;
			}
		}
		
		if (itemId == 'step-2'){
			var field = this.lookupReference('message');
			if (field.getValue()== null || field.getValue().length<4){
				Ext.Msg.alert('Ошибка', 'Введите текст сообщения (минимум 4 символа)');
				return;
			}
		}
		
		if (itemId == 'step-3'){
			
			var store = this.lookupReference('gridPhones').getStore();
			var cntRec = store.getCount()
			
			if (cntRec == 0){
				/* Выдать собщение, что не указан ни один телефон! */
				Ext.Msg.alert('Ошибка', 'Добавьте получателей');
				return;
			}
			
			if (cntRec == 1){
				var descriptionField = this.lookupReference('description');
				descriptionField.setValue('');
				layout.setActiveItem('step-4');
			};			
		};
		
		if (itemId == 'step-4'){
			var field = this.lookupReference('description');
			if (field.getValue()== null || field.getValue().length<4){
				Ext.Msg.alert('Ошибка', 'Укажите описание рассылки');
				return;
			}
		}
		this.navigate(layout, "next");
	},	
	
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
			var cnt = store.getCount();
			
			if (cnt + validPhones.length >1000){
				Ext.Msg.alert('Внимание', 'Максимальное количество получателей - 1000!');
			}		
			
			validPhones.forEach(function(phone, index, array){
				if (cnt<1000 && store.find("phone", phone)==-1){
					 store.add({phone:phone});
					 cnt++;
				 }
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
		
		var phoneStore = me.lookupReference('gridPhones').getStore();
		if (phoneStore.getCount()==0){		
			return;
		}
		
		if (phoneStore.getCount() >1000){
			Ext.Msg.alert('Внимание', 'Максимальное количество получателей - 1000!');
			return;
		}		

		
		
		var channelsStore = me.lookupReference('gridChannels').getStore();
		if (channelsStore.getCount()==0){
			Ext.Msg.alert('Ошибка', 'Добавьте канал');
			return;
		}		

		var channels = '';
		channelsStore.each(function(rec) { channels += rec.get('channel'); });

		var fieldValues = dataPanel.getForm().getFieldValues();
		var message = {};
		var requestData = {
				channels: channels,
				messages:[]
			};
		
		if (phoneStore.getCount()>1) Ext.apply(requestData, {description:fieldValues.description});		
		
		
		
		phoneStore.each(function(rec) {
			
			message = {
				text:fieldValues.text,				
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
						var rec = phoneStore.findRecord('phone', item.messageId);
						phoneStore.remove(rec);
						phoneStore.commitChanges();
					});
					
					var layout = me.lookupReference('dataPanel').getLayout();
					
					if (jResponse.failedMessages.length>0){
						layout.setActiveItem('step-3');
					} else{
						layout.setActiveItem('step-1');
					}
					
				} else {
					Ext.Msg.alert('Ошибка', jResponse.message);
				}
				me.getView().getEl().unmask();
			},
			failure: function(a,b,c){
				me.getView().getEl().unmask();
			}
		});	
	},
	btnAddChannel: function(){
		var me = this;
		var panel = Ext.create('opers.view.AddChannel',{
			listeners:{
				addChannel: function(panel, record){
					var store = me.lookupReference('gridChannels').getStore();
					store.add(record);
				}
			}
		});
		panel.show();
	},
	btnDeleteChannel:function(grid, rowIndex, colIndex) {
		var rec = grid.getStore().getAt(rowIndex);
		Ext.MessageBox.confirm('Подтверждение удаления', 'Вы уверены, что хотите удалить канал '+rec.data.channel+' ?', function(btn){
			if (btn === 'yes') {
				grid.getStore().removeAt(rowIndex);
			}
		});
	},
	btnDeletePhone:function(grid, rowIndex, colIndex) {
		var rec = grid.getStore().getAt(rowIndex);
		Ext.MessageBox.confirm('Подтверждение удаления', 'Вы уверены, что хотите удалить телефон '+rec.data.phone+' ?', function(btn){
			if (btn === 'yes') {
				grid.getStore().removeAt(rowIndex);
			}
		});
	}
});

Ext.define('opers.controller.AddChannel', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.AddChannelController',
	
	btnAdd: function(){

		var me = this;
		var panel = this.getView();
		if (panel.isValid()){
			
			var fieldValues = panel.getForm().getFieldValues();
			var newChannel  = Ext.create('opers.model.Channel',{
				channel:fieldValues.channel
			});			
			
			panel.fireEvent('addChannel', panel, newChannel);
			panel.close();
		}		
	},
	btnClose:function(){
		this.getView().destroy();
	}
});
