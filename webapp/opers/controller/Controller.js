Ext.define('opers.controller.Controller', {
	extend: 'Ext.app.Controller',

	views: ['Messages','SendOneToAll','PhoneGrants'],
	stores:['Messages', 'MessageTypes', 'Scenarios', 'Reports','PhoneGrants','Bulks','Channels'],
	models:['Message','MessageType', 'Scenario', 'Report','PhoneGrant','Bulk','Channel'],

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
	        }
	    });
	},
	
	openMessagesPanel: function(){	
		this.app.setDataPanel (this.getMessagesView().create());
	},
	
	openSendOneToAll: function(){	
		this.app.setDataPanel (this.getSendOneToAllView().create());
	},
	
	openPhoneGrants: function(){	
		this.app.setDataPanel (this.getPhoneGrantsView().create());
	},
	
	
	
	
});
