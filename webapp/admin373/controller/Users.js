Ext.define('admins.controller.Users', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.UsersController',
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
	roleRenderer: function(role){
		var rec = admins.getApplication().controller.getUserRolesStore().findRecord('role', role);
		return (rec?role+' ('+rec.get('description')+')':null);
	},
	
	typeIdRenderer: function(typeId){
		var store = Ext.data.StoreManager.lookup('messageTypesStore')
		var rec = store.findRecord('typeId', typeId);
		return (rec?typeId+' ('+rec.get('description')+')':typeId);
	},
	
	//Events
	
	selectGrid: function(grid, record){
		var me = this,
			tabPanel = me.lookupReference('tabPanel');
			dataPanel = me.lookupReference('dataPanel');
		me.currentRecord = record;		
		tabPanel.expand();
		dataPanel.getForm().setValues(record.data);
		var roles = [];
		Ext.Array.each(record.data.roles, function(item){
			roles.push([item]);
		});
		me.lookupReference('rolesGrid').getStore().loadRawData(roles);
		
		var mtStore = new Ext.create('Ext.data.Store',{
			model:'admins.model.UserMessageType',
			autoLoad:false,
			autoSync:false
		});
		mtStore.load({params:{userId:record.get('id')}});
		me.lookupReference('messagetypesGrid').bindStore(mtStore);
	},
	
	//Buttons	
	
	btnAdd: function(){
		var me = this;
		var panel = Ext.create('admins.view.AddUser',{
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
				title:'Удаление пользователя',
				msg: 'Вы уверены, что хотите удалить пользователя "'+record.get('userName')+'"?',
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
			
			fieldValues.roles = [];

			me.lookupReference('rolesGrid').getStore().each(function(rec) {
					fieldValues.roles.push(rec.get('role'));
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
	
	btnAddRole: function(){
		var me = this;
		var panel = Ext.create('admins.view.AddUserRole',{
			listeners:{
				addRole: function(panel, role){
					var store = me.lookupReference('rolesGrid').getStore();
					
					var rec = store.findRecord('role',role);
					if (!rec) store.add([[role]]);
				}
			}
		});
		panel.show();
	},
	btnDeleteRole:function(grid, rowIndex, colIndex) {
		var rec = grid.getStore().getAt(rowIndex);
		Ext.MessageBox.confirm('Подтверждение удаления', 'Вы уверены, что хотите отобрать у пользователя роль '+rec.data.role+' ?', function(btn){
			if (btn === 'yes') {
				grid.getStore().removeAt(rowIndex);
			}
		});
	},	
	
	btnAddMessageType: function(){
		var me = this;
		var panel = Ext.create('admins.view.AddUserMessageType',{
			listeners:{
				addMessageType: function(panel, fieldValues){
					
					var store = me.lookupReference('messagetypesGrid').getStore();
					var rec = store.findRecord('typeId',fieldValues.typeId);
					
					if (!rec){
					
						var newUserMessageType = Ext.create('admins.model.UserMessageType', {
							typeId: fieldValues.typeId,
							userId: me.currentRecord.getId()
						});
						
						/* Поскольку мы заполними Id записи в typeId, то движок будет думать, что это серверная запись и
						 * будет запускать update, а не create. Для этого помечаем запись как фантомную, чтобы она сохранялась как новая через create */
						newUserMessageType.phantom=true;
						me.getView().getEl().mask('Добавление доступа к типу сообщения...');
						newUserMessageType.save({
							success:function(){
								panel.fireEvent('afterSave', panel, newUserMessageType);	
								store.add(newUserMessageType);
							},
							callback:function(){
								me.getView().unmask();
							}
						});
					}
				}
			}
		});
		panel.show();
	},
	btnDeleteMessageType:function(grid, rowIndex, colIndex) {
		var me = this,
			store = grid.getStore(),
			rec = store.getAt(rowIndex);
			
		Ext.MessageBox.confirm('Подтверждение удаления', 'Вы уверены, что хотите отобрать у пользователя доступ к типу сообщения '+rec.data.typeId+' ?', function(btn){
			if (btn === 'yes') {				
				
				store.removeAt(rowIndex);
				me.getView().getEl().mask('Сохранение данных...');
				store.sync({
					failure:function(){
						store.rejectChanges();
						me.getView().getEl().unmask();
					},
					success: function(){
						store.commitChanges();
						me.getView().getEl().unmask();
					}
				});	
			}
		});
	},
	
});


Ext.define('admins.controller.AddUser', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.AddUserController',
	btnAdd: function(){

		var panel = this.getView();
		if (panel.isValid()){
			
			var fieldValues = panel.getForm().getFieldValues();
			var newUser  = Ext.create('admins.model.User',{
				id:0,
				userName:fieldValues.userName,
				login:fieldValues.login,
				locked:false,
				password:fieldValues.password
			});			
			
			newUser.phantom = true;
			panel.getEl().mask('Создание пользователя...');
			newUser.save({
				success:function(){
					panel.fireEvent('afterSave', panel, newUser);
					
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

Ext.define('admins.controller.AddUserRole', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.AddUserRoleController',
	btnAdd: function(){

		var me = this;
		var panel = this.getView();
		if (panel.isValid()){
			
			var fieldValues = panel.getForm().getFieldValues();		
			panel.fireEvent('addRole', panel, fieldValues.role);
			panel.close();
		}		
	},
	btnClose:function(){
		this.getView().destroy();
	}
});

Ext.define('admins.controller.AddUserMessageType', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.AddUserMessageTypeController',
	btnAdd: function(){

		var me = this;
		var panel = this.getView();
		if (panel.isValid()){
			
			var fieldValues = panel.getForm().getFieldValues();					
			panel.fireEvent('addMessageType', panel, fieldValues);
			panel.close();
		}		
	},
	btnClose:function(){
		this.getView().destroy();
	}
});
