Ext.define('opers.view.Messages', { 	
	extend:'Ext.Container',
	requires:['opers.store.MessageTypes'],
	itemId:'messagesPanel',
	cls:'messages-panel',
	layout:{type:'vbox', align:'stretch'},
	items:[ 
		{	html:'<div class="page-header">Сообщения</div>',
			height:50
		},
		{	xtype:'form',
			itemId: 'filterPanel',
			height:50,
			layout:{type:'hbox'},
			defaults:{margin:'10 5 0 5', labelWidth:110, labelAlign:'right', width:230},
			items:[
				{	xtype:'datefield',
					name:'beginDate',
					cls:'message-filter-field',
					fieldLabel:'Начальная дата',
					format:'d.m.Y',
					submitFormat:'Y-m-d',					
				},
				{	xtype:'datefield',
					name:'endDate',
					cls:'message-filter-field',
					fieldLabel:'Конечная дата',
					format:'d.m.Y',
					submitFormat:'Y-m-d',
					
				},
				{	xtype:'textfield',
					name:'phone',
					cls:'message-filter-field',
					fieldLabel:'Номер телефона',
					enforceMaxLength:true,
					maxLength:11,
					minLength:11,
					maskRe:/^([0-9]{0,11})$/
				},
				{	xtype:'combobox',
					name:'messageType',
					cls:'message-filter-field',
					store:'messageTypesStore',
					queryMode: 'local',
					displayField: 'description',
					valueField: 'typeId',
					fieldLabel:'Тип сообщения',
					editable:false,
					width:350
				},				
				{	xtype:'combobox',
					name:'bulkId',
					cls:'message-filter-field',
					store:'bulksStore',
					queryMode: 'remote',
					displayField: 'description',
					valueField: 'id',
					fieldLabel:'Рассылка',
					width:350
				},
				{	xtype:'button',
					cls:'message-filter-button',
					itemId: 'btnMessagesFilter',
					text:'Фильтровать',
					width:100
				},
				{	xtype:'button',
					cls:'message-filter-button',
					itemId: 'btnMessagesFilterClear',
					text:'Очистить',
					width:100
				}
			]
		},
		{
			xtype: 'container',
			flex:1,
			padding:'5 5 5 5',
			layout: {type:'hbox', align:'stretch'},
			items:[
				{	xtype: 'grid',
					itemId: 'gridPanel',
					cls:'messages-grid-panel',
					flex:1,
					border:1,
					store:'messagesStore',
					columns:[
						{ text: 'Телефон',  dataIndex: 'phoneNumber' },
						{ text: 'Текст', dataIndex: 'smsText', flex: 1},
						{ text: 'Внешний Id',  dataIndex: 'externalId',width:150},
						{ text: 'Создано',  dataIndex: 'createDate',width:150, 
							renderer:function(value){
								return Ext.Date.format(value, 'd.m.Y H:i:s');
							}
						}					
					],
					bbar: { 
						xtype: 'pagingtoolbar',
						displayInfo: true,
						store:'messagesStore'
					}
				},
				{	xtype:'splitter'},
				{	xtype:'form',
					flex:1,
					cls:'message-data-panel',
					minWidth:600,
					itemId: 'dataPanel',
					collapsible:true,
					collapsed:true,
					collapseDirection:'right',
					title:'Данные сообщения',
					border:1,
					layout:{type:'vbox', align:'stretch'},
					bodyPadding:'5 5 5 5',
					defaults:{anchor:'100%', labelWidth:150, labelAlign:'right', margin:'0 5 0 5'},
					items:[
						{	xtype:'displayfield',
							name: 'id',
							itemId:'id',
							cls:'message-data-field',
							fieldLabel:'Id'
						},
						{	xtype:'displayfield',
							name: 'createDate',
							itemId:'createDate',
							cls:'message-data-field',
							fieldLabel:'Время создания',
							renderer:function(value){
								return Ext.Date.format(value, 'd.m.Y H:i:s');
							}
						},
						{	xtype:'displayfield',
							name: 'externalId',
							itemId:'externalId',
							cls:'message-data-field',
							fieldLabel:'Внешний Id'
						},
						{	xtype:'displayfield',
							name: 'smsText',
							itemId:'smsText',
							cls:'message-data-field',
							fieldLabel:'SMS'
						},
						{	xtype:'displayfield',
							name: 'viberText',
							itemId:'viberText',
							cls:'message-data-field',
							fieldLabel:'Viber'
						},
						{	xtype:'displayfield',
							name: 'voiceText',
							itemId:'voiceText',
							cls:'message-data-field',
							fieldLabel:'Voice'
						},
						{	xtype:'displayfield',
							name: 'parsecoText',
							itemId:'parsecoText',
							cls:'message-data-field',
							fieldLabel:'Parseco'
						},
						
						{	xtype:'displayfield',
							name: 'user',
							itemId:'user',
							cls:'message-data-field',
							fieldLabel:'Создал сообщение'
						},
						
						{	xtype:'displayfield',
							name: 'scenario',
							itemId:'scenario',
							cls:'message-data-field',
							fieldLabel:'Сценарий'
						},
						{
							xtype: 'grid',
							itemId:'reportsGridPanel',
							store:'reportsStore',
							flex:1,
							border:1,
							autoScroll:true,
							columns:[
								{ text: 'report',  dataIndex: 'reportDate', xtype:'datecolumn', width:150, align:'center', format:'d.m.Y H:i:s'},
								{ text: 'doneAt',  dataIndex: 'doneAt', xtype:'datecolumn', width:150, align:'center', format:'d.m.Y H:i:s'},
								{ text: 'Канал',  dataIndex: 'channel', width:100, align:'center'},
								{ text: 'Группа',  dataIndex: 'statusGroup', width:180},
								{ text: 'Статус',  dataIndex: 'statusName', width:300},								
								{ text: 'Описание',  dataIndex: 'statusDescription', flex:1, minWidth:400},
								{ text: 'Количество',  dataIndex: 'messageCount', width:110, align:'right'},
								{ text: 'Цена',  dataIndex: 'pricePerMessage', width:100, align:'right'},
								{ text: 'Валюта',  dataIndex: 'priceCurrency', width:100, align:'center'},
								{ text: 'sentAt',  dataIndex: 'sentAt', xtype:'datecolumn', width:150, align:'center', format:'d.m.Y H:i:s'},
							]
						}
					]
				}
			]
		}
	
	]
});
