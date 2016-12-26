Ext.define('opers.controller.Messages', { 		
	extend: 'Ext.app.ViewController',
	alias: 'controller.MessagesController',
	init: function(){
		this.lookupReference('gridPanel').getStore().load();
		this.btnFilter();
	},
	
	// Renderers
	checkboxRenderer: function(value){ if (value) return 'V'; else return 'X';},
	
	//Events
	
	selectGrid: function(grid, record){
		var me = this,
			dataPanel = me.lookupReference('dataPanel'),
			reportsGrid = me.lookupReference('reportsGrid');

		me.currentRecord = record;	
		
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
			
		dataPanel.expand();
		dataPanel.getForm().setValues(data);
		reportsGrid.getStore().getProxy().extraParams = {messageId:data.id};
		reportsGrid.getStore().load();
	},
	
	//Buttons	
	
	btnFilter: function(){
		var filterPanel = this.lookupReference('filterPanel'),
			gridPanel = this.lookupReference('gridPanel');
			
		if (filterPanel.isValid()){
			var fieldValues = filterPanel.getForm().getFieldValues();
			var store = gridPanel.getStore();
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
			
		}
	},
	btnFilterClear: function(){
		this.looupReference('filterPanel').reset();
	},
	
		
});
