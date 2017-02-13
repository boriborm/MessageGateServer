Ext.define('admins.controller.Processors', {
	extend: 'Ext.app.ViewController',
	alias: 'controller.ProcessorsController',
	// Renderers
	//Events
	//Buttons	
	
	htmlTemplate: new Ext.XTemplate(
		'<div class="processor-info">',
		'<span class="processor-active-{active}"></span>',
		'<span class="processor-status">{status}</span>',
		'</div>'
	),	
	getStatus:function(){
		var me = this;
		this.request('status', function(response){
			console.log(response);
			var html = me.htmlTemplate.applyTemplate(response.queue);
			me.lookupReference('queueStatus').update(html);
			html = me.htmlTemplate.applyTemplate(response.file);
			me.lookupReference('fileStatus').update(html);
			html = me.htmlTemplate.applyTemplate(response.delivery);
			me.lookupReference('deliveryStatus').update(html);
			me.getView().getViewModel().set('queueIsActive', response.queue.active);
			me.getView().getViewModel().set('fileIsActive', response.file.active);
			me.getView().getViewModel().set('deliveryIsActive', response.delivery.active);
		})
	},
	
	startQueue:function(){
		var me = this;
		this.request('startQueue', function(response){
			me.getStatus();
		})
	},
	
	stopQueue:function(){
		var me = this;
		this.request('stopQueue', function(response){
			me.getStatus();
		})
	},

	startFile:function(){
		var me = this;
		this.request('startFile', function(response){
			me.getStatus();
		})
	},
	
	stopFile:function(){
		var me = this;
		this.request('stopFile', function(response){
			me.getStatus();
		})
	},
	startDelivery:function(){
		var me = this;
		this.request('startDelivery', function(response){
			me.getStatus();
		})
	},
	
	stopDelivery:function(){
		var me = this;
		this.request('stopDelivery', function(response){
			me.getStatus();
		})
	},
	
	//functions	
	request: function(command, callback){
		var me = this;
		me.getView().getEl().mask("Запрос информации...");
		Ext.Ajax.request({
			url: '/rest/processors/'+command,
			method:'GET',
			success: function(response){
				
				var jResponse = Ext.decode(response.responseText);
				
				if (jResponse.success){
					callback(jResponse);
				}else{
					Ext.Msg.alert('Ошибка', jResponse.message);
				};
				
				me.getView().getEl().unmask();
			},
			failure: function(){
				me.getView().getEl().unmask();
			}
		});	
	}

});
