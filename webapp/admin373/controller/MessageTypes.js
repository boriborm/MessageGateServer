Ext.define('admins.controller.MessageTypes', {
	extend: 'Ext.app.ViewController',
	alias: 'controller.MessageTypesController',
	init: function(){
		this.lookupReference('gridPanel').getStore().load();
	},
	// Renderers
	checkboxRenderer: function(value){ if (value) return 'V'; else return 'X';},
	
	//Events
	
	selectGrid: function(grid, record){
		var me = this,
			dataPanel = me.lookupReference('dataPanel');
		me.currentRecord = record;		
		dataPanel.expand();
		dataPanel.getForm().setValues(record.data);
	},
	
	//Buttons	
	
	btnAdd: function(){
		var me = this;
		var panel = Ext.create('admins.view.AddMessageType',{
			listeners:{
				afterSave:function(panel, record){						
					var store = me.lookupReference('gridPanel').getStore().add(record);
				}
			}
		});
		panel.show();
	},
	
	btnDelete: function(){
		var me = this,
			record = me.currentRecord;
			
		if (record){
			Ext.MessageBox.show({
				title:'Удаление типа сообщения',
				msg: 'Вы уверены, что хотите удалить тип сообщения "'+record.get('typeId')+'"?',
				buttons: Ext.MessageBox.YESNO,
				icon: Ext.MessageBox.QUESTION,    // иконка мб {ERROR,INFO,QUESTION,WARNING}
				width:300,                       // есть еще minWidth
				closable:false,                  // признак наличия икнки закрытия окна
				buttonText: {yes: 'Да', no: 'Нет'},
				fn: function(btn){
					if (btn=='yes'){
						var store = me.lookupReference('gridPanel').getStore();
						me.getView().getEl().mask('Сохранение данных...');
						store.remove(record);
						store.sync({
							failure:function(){
								store.rejectChanges();
								me.getView().getEl().unmask();
							},
							success:function(){
								me.currentRecord = null;
								me.getView().getEl().unmask();
							}
						});						
					}
				}
			});
		}
	},
	
	btnSave:function(){
		var me = this;
		var dataPanel = me.lookupReference('dataPanel');
		var fieldValues = dataPanel.getForm().getFieldValues();
		var store = me.lookupReference('gridPanel').getStore();
		var record=store.getById(me.currentRecord.getId());
		if (record&&dataPanel.isValid()){
								
			record.set(fieldValues);
			me.currentRecord = record;			
			if (record.dirty) me.getView().getEl().mask('Сохранение данных...');
			store.sync({
				failure:function(){
					store.rejectChanges();
					me.getView().getEl().unmask();
				},
				success: function(){
					me.getView().getEl().unmask();
				}
			});			
		}		
	}
});


Ext.define('admins.controller.AddMessageType', {
	extend: 'Ext.app.ViewController',
	alias: 'controller.AddMessageTypeController',
	btnAdd: function(){

		var me = this;
		var panel = this.getView();

		if (panel.isValid()){
			
			var fieldValues = panel.getForm().getFieldValues();
			
			var newMessageType = Ext.create('admins.model.MessageType', {
				typeId: fieldValues.typeId,
				description: fieldValues.description,
				acceptSms: false,
				acceptViber: false,
				acceptVoice: false,
				acceptParseco: false,
				active: false
			});
			/* Поскольку мы заполними Id записи в typeId, то движок будет думать, что это серверная запись и
			 * будет запускать update, а не create. Для этого помечаем запись как фантомную, чтобы она сохранялась как новая через create */
			newMessageType.phantom=true;
			panel.getEl().mask('Создание сценария...');
			newMessageType.save({
				success:function(){
					panel.fireEvent('afterSave', panel, newMessageType);	
				},
				callback:function(){
					panel.getEl().unmask();
					panel.destroy();
				}
			});
		}
	},
	btnDelete:function(){
		var me = this,
			record = this.currentRecord;
			
		if (record){
			Ext.MessageBox.show({
				title:'Удаление Типа сообщения',
				msg: 'Вы уверены, что хотите удалить тип сообщения "'+record.get('typeId')+'"?',
				buttons: Ext.MessageBox.YESNO,
				icon: Ext.MessageBox.QUESTION,    // иконка мб {ERROR,INFO,QUESTION,WARNING}
				width:300,                       // есть еще minWidth
				closable:false,                  // признак наличия икнки закрытия окна
				buttonText: {yes: 'Да', no: 'Нет'},
				fn: function(btn){
					if (btn=='yes'){
						var store = me.lookupReference('gridPanel').getStore();
						store.remove(record);
						store.sync({
							failure:function(){
								store.rejectChanges();
							}
						});						
					}
				}
			});
		}
	},
	btnClose:function(){
		this.getView().destroy();
	}
});
