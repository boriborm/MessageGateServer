Ext.define('admins.controller.Controller', {
	extend: 'Ext.app.Controller',

	views: ['MessageTypes','Scenarios','Users'],
	stores:['MessageTypes','Scenarios','Users','UserRoles'],
	models:['MessageType','Scenario','User'],

	init: function(){
		var me = this;
		
		me.control({
			'#btnToolbarPreferences': {
            	click: me.openPreferencesPanel
	        },
	        '#btnToolbarScenarios': {
            	click: me.openScenariosPanel
	        },	        
	        '#btnToolbarMessageTypes': {
            	click: me.openMessageTypesPanel
	        },
	        '#btnToolbarUsers': {
            	click: me.openUsersPanel
	        }			
		});
	},
		
	openScenariosPanel: function(){	
		var me = this;		
		me.app.setDataPanel (me.scenariosPanel);
	},
	openMessageTypesPanel: function(){	
		var me = this;
		me.app.setDataPanel (me.messageTypesPanel);
	},
	openUsersPanel: function(){	
		var me = this;
		me.app.setDataPanel (me.usersPanel);
	}	
	
});
