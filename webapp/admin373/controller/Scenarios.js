Ext.define('admins.controller.Scenarios', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.ScenariosController',
	init: function(){
		this.lookupReference('gridPanel').getStore().load();
	},
	// Renderers
	checkboxRenderer: function(value){ if (value) return 'V'; else return 'X';},
	channelsRenderer:function(channels){
		var output = '';
		Ext.Array.each(channels, function(item){
			output = output + item.channel+" -> ";
		});
		if (output) return output.substr(0,output.length-4);
		return '';
	},
	
	//Events
	
	selectGrid: function(grid, record){
		var me = this,
			dataPanel = me.lookupReference('dataPanel');
		me.currentRecord = record;		
		dataPanel.expand();
		dataPanel.getForm().setValues(record.data);
		// Заполняем стор для каналов
		var channelsStore = new Ext.create('Ext.data.Store',{
			model: 'admins.model.ScenarioFlow'
		});
		if (record.data.flow) channelsStore.loadData(record.data.flow);
		me.lookupReference('channelsGrid').reconfigure(channelsStore);
	},
	
	//Buttons	
	
	btnAdd: function(){
		var me = this;
		var panel = Ext.create('admins.view.AddScenario',{
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
				title:'Удаление сценария',
				msg: 'Вы уверены, что хотите удалить сценарий "'+record.get('name')+'"?',
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
							success: function(){
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
			fieldValues.flow = [];

			me.lookupReference('channelsGrid').getStore().each(function(rec) {
					fieldValues.flow.push(rec.data);
			});

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
	},
	
	btnAddChannel: function(){
		var me = this;
		var panel = Ext.create('admins.view.AddScenarioChannel',{
			listeners:{
				addChannel: function(panel, record){
					var store = me.lookupReference('channelsGrid').getStore();
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
	}
});


Ext.define('admins.controller.AddScenarios', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.AddScenarioController',
	btnAdd: function(){

		var panel = this.getView();
		if (panel.isValid()){
			
			var fieldValues = panel.getForm().getFieldValues();
			var newScenario  = Ext.create('admins.model.Scenario',{
				id:0,
				name:fieldValues.name,
				active:false,
				isDefault:false,
				flow:[{channel:'SMS',from:'MGS'}],
				key:null
			});			
			
			newScenario.phantom = true;
			panel.getEl().mask('Создание сценария...');
			newScenario.save({
				success:function(){
					panel.fireEvent('afterSave', panel, newScenario);
					
				},
				callback:function(){
					panel.getEl().unmask();
					panel.destroy();
				}
			});			
			
		}
	},
	btnClose:function(){
		this.getView().destroy();
	}
});

Ext.define('admins.controller.AddScenarioChannel', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.AddScenarioChannelController',
	btnAdd: function(){

		var me = this;
		var panel = this.getView();
		if (panel.isValid()){
			
			var fieldValues = panel.getForm().getFieldValues();
			var newChannel  = Ext.create('admins.model.ScenarioFlow',{
				channel:fieldValues.channel,
				from:fieldValues.from
			});			
			
			panel.fireEvent('addChannel', panel, newChannel);
			panel.close();
		}		
	},
	btnClose:function(){
		this.getView().destroy();
	}
});
