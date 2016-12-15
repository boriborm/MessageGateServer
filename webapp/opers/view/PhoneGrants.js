Ext.define('opers.view.PhoneGrants', {
	extend:'Ext.Container',
	itemId:'phoneGrantsPanel',
	cls:'phone-grants-panel',
	layout: {type:'vbox',align:'stretch'},
	items:[ 
		{	html:'<div class="page-header">Настройка доступа телефона к каналам</div>',
			height:50
		},
		{	xtype:'form',
			padding:'5 5 5 5',
			itemId: 'dataPanel',
			flex:1,
			autoScroll:true,
			layout:{type:'vbox', align:'center'},
			defaults:{margin:'5 5 5 5', labelWidth:110, labelAlign:'right'},
			items:[
				{	xtype:'combobox',
					name:'phoneNumber',
					itemId:'phoneNumber',
					cls:'data-field',
					store:'phoneGrantsStore',
					queryMode: 'remote',
					displayField: 'phoneNumber',
					valueField: 'phoneNumber',
					fieldLabel: 'Номер телефона',
					allowBlank:false,
					hideTrigger:true,
					minLength:11,
					maxLength:11,
					enforceMaxLength:true
				},
				{	xtype:'fieldcontainer',
					layout:'hbox',
					fieldLabel: 'Доступные каналы',
					labelWidth:120,
					padding:' 5 5 5 5',
					defaults:{margin:'0 10 0 10', inputValue:1, xtype:'checkbox'},
					items:[
						{	boxLabel  : 'SMS',
							name:'acceptSms',
							itemId:'acceptSms'
						},
						{	boxLabel  : 'Viber',
							name:'acceptViber',
							itemId:'acceptViber'
						},
						{	boxLabel  : 'Voice',							
							name:'acceptVoice',
							itemId:'acceptVoice'
						},
						{	boxLabel  : 'Parseco',
							name:'acceptParseco',
							itemId:'acceptParseco'
						}
					]
				},
				{	xtype:'container',
					layout: {type:'hbox', align:'center'},
					padding:' 5 5 5 5',
					defaults:{margin:'0 10 0 10', inputValue:1, xtype:'checkbox'},
					items:[
						{	xtype:'button',
							itemId:'btnSavePhoneGrants',
							text:' Сохранить',
							disabled:true
						}
					]
				}
			]
		}	
	]
});
