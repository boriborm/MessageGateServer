Ext.define('admins.controller.Controller', {
	extend: 'Ext.app.Controller',

	views: ['MessageTypes','Scenarios','Users','Processors'],
	stores:['MessageTypes','Scenarios','Users','UserRoles'],
	models:['MessageType','Scenario','User','UserMessageType'],

	init: function(){
		var me = this;
		
		me.control({
	        '#btnToolbarScenarios': {
            	click: me.openScenariosPanel
	        },	        
	        '#btnToolbarMessageTypes': {
            	click: me.openMessageTypesPanel
	        },
	        '#btnToolbarUsers': {
            	click: me.openUsersPanel
	        },
	        '#btnToolbarProcessors': {
            	click: me.openProcessorsPanel
	        }
		});
	},
		
	openScenariosPanel: function(){	
		var me = this;
		me.app.setDataPanel (me.getScenariosView().create());
	},
	
	openMessageTypesPanel: function(){	
		var me = this;		
		me.app.setDataPanel (me.getMessageTypesView().create());
	},
	
	openUsersPanel: function(){	
		var me = this;
		me.app.setDataPanel (me.getUsersView().create());
	},
	openProcessorsPanel: function(){	
		var me = this;
		me.app.setDataPanel (me.getProcessorsView().create());
	}
	
});
