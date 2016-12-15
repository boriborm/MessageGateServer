Ext.define('opers.view.SendOneToAll', {
	extend:'Ext.Container',
	itemId:'sendOneToAllPanel',
	cls:'send-one-to-all-panel',
	layout: {type:'vbox',align:'stretch'},
	items:[ 
		{	html:'<div class="page-header">Отправка сообщения</div>',
			height:50
		},
		{	xtype:'form',
			itemId: 'dataPanel',
			flex:1,
			padding:'5 5 5 5',
			layout:{type:'vbox', align:'center'},
			defaults:{margin:'5 5 5 5', labelWidth:110, labelAlign:'right'},
			items:[
				{	xtype:'combobox',
					name:'scenario',
					itemId:'scenario',
					cls:'send-data-field',
					store:'scenariosStore',
					queryMode: 'local',
					displayField: 'name',
					valueField: 'key',
					fieldLabel: 'Сценарий',
					editable:false,
					allowBlank:false,
					width:600
				},
				{	xtype:'textarea',
					name:'text',
					itemId:'text',
					
					cls:'send-data-field',
					fieldLabel:'Сообщение',
					enforceMaxLength:true,
					maxLength:1000,
					minLength:4,
					allowBlank:false,
					grow: true,
					width:600
				},
				
				{	xtype:'textfield',
					name:'description',
					itemId:'description',					
					cls:'send-data-field',
					fieldLabel:'Описание рассылки',
					enforceMaxLength:true,
					maxLength:100,
					minLength:3,
					allowBlank:false,
					disabled:true,
					width:600
				},				
				{	xtype:'fieldcontainer',
					layout:'hbox',
					fieldLabel: 'Рассылать по каналам',
					labelWidth:150,
					padding:' 5 5 5 5',
					defaults:{margin:'0 10 0 10', inputValue:1, xtype:'checkbox'},
					items:[
						{	boxLabel  : 'SMS',
							name:'toSms',
							itemId:'toSms'
						},
						{	boxLabel:'Viber',
							name:'toViber',
							itemId:'toViber'
						},
						{	boxLabel  : 'Voice',							
							name:'toVoice',
							itemId:'toVoice'
						},
						{	boxLabel  : 'Parseco',
							name:'toParseco',
							itemId:'toParseco'
						}
					]
				},
				{	xtype:'container',
					layout:  {type:'hbox', align:'stretch'},
					defaults:{padding:'5 5 5 5'},					
					flex:1,
					items:[
						{	xtype:'container',
							layout: {type:'vbox', align:'center',pack:'start'},
							flex:1,
							border:1,
							defaults:{margin:'5 5 5 5'},
							items:[
								{	xtype:'textareafield',
									name:'phones',
									cls:'send-data-field',
									fieldLabel:'Номера телефонов',
									labelAlign:'top',
									grow:true,
									width:120,
									maskRe:/^([0-9]{0,11})$/
								},
								{	xtype:'button',
									cls:'message-filter-button',
									itemId: 'btnAddPhone',
									text:'Добавить',
									width:100
								},
								{	xtype:'button',
									itemId:'btnStart',
									text:'Запустить рассылку',
									margin:'60 0 0 0'
								}
							]
						},						
						{	xtype:'container',
							layout: 'fit',
							flex:1,
							items:[
								{	xtype: 'grid',
									itemId: 'gridPhones',
									cls:'send-grid-panel',							
									width:200,
									border:1,
									autoScroll:true,
									forceFit:true,
									store: new Ext.create('Ext.data.ArrayStore', {
										fields: [
											{name: 'phone', type: 'string'}
										]
									}),
									columns:[
										{ text: 'Телефон',  dataIndex: 'phone' }
									]
								}
							]
						}
					]
				},
				
			]
		}	
	]
});
