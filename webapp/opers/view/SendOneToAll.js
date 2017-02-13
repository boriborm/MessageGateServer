Ext.define('opers.view.SendOneToAll', {
	extend:'Ext.Container',
	itemId:'sendOneToAllPanel',
	requires:['opers.controller.SendOneToAll'],
	controller: 'SendOneToAllController',
	cls:'send-one-to-all-panel',
	layout: {type:'vbox',align:'stretch'},
	items:[ 
		{	html:'<div class="page-header">Отправка сообщения</div>',
			height:50
		},
		{	xtype:'form',
			reference: 'dataPanel',
			flex:1,
			padding:'5 50 5 50',
			layout:'card',
			bbar: [
				{
					reference: 'btnMovePrev',
					text: 'Назад',
					handler: 'goBack',
					disabled: true,
					scale:'large',
					margin:30
				},
				'->', // greedy spacer so that the buttons are aligned to each side
				{
					reference: 'btnMoveNext',
					text: 'Далее',
					handler: 'goNext',
					scale:'large',
					margin:30
				}
			],
			defaults:{
				header:{height:50, cls:'big-header'}
			},
			items:[
				{   id:'step-1',
					title:'Шаг 1. Выберите каналы доставки',
					
					defaults:{padding:50},
					layout: {type:'hbox', align:'center', pack:'center'},
					items:[
								{	xtype:'grid',
									reference: 'gridChannels',
									model:'opers.model.Channel',
									store: new Ext.create('Ext.data.Store',{
										model: 'opers.model.Channel'
									}),
									width:400,
									height:300,
									border:1,
									sortableColumns:false,
									enableColumnHide:false,
									enableColumnMove:false,
									viewConfig: {
										plugins: {
											ptype: 'gridviewdragdrop'
										},
										listeners:{
											drop:function(node, data, overModel, dropPosition){
												if (data.records.length==1){
													var rec = data.records[0]
													this.refresh();
												}
											},
											beforedrop: function(node, data, overModel, dropPosition, dropHandlers){
												dropHandlers.wait = true;
												Ext.MessageBox.confirm('Подтверждение перемещения', 'Вы уверены?', function(btn){
													if (btn === 'yes') {
														dropHandlers.processDrop(); 									
													} 
													else dropHandlers.cancelDrop();
												});
											}
										}
									},
									columns: [
										{ text: 'Канал',  dataIndex: 'channel', width:100, flex:1, renderer: 'channelRenderer'},
										{	xtype:'actioncolumn',
											width:40,
											align:'center',
											items: [{
												icon: '/resources/images/delete.png',
												tooltip: 'Удалить',
												handler: 'btnDeleteChannel'
											}]
										}
									],
									tbar:[
										{	xtype:'button',
											text:'Добавить / обновить канал',
											handler:'btnAddChannel',
											scale:'medium'
										}
									]
								},
								{	xtype:'container',
									flex:1,
									maxWidth:500,
									cls:'description',
									html:'Добавьте каналы для осуществления рассылки. Укажите приоритет каналов передвигая выбранные каналы в порядке предпочтительного использования.'
								}
					]
				},
				{   id:'step-2',
					title:'Шаг 2. Укажите текст сообщения',					
					layout: {type:'vbox', align:'center', pack:'center'},					
					items:[
						{	xtype:'container',
							flex:1,
							maxWidth:500,
							maxHeight:30,
							cls:'description',
							html:'Укажите текст сообщения'
						},
						{	xtype:'textarea',
							name:'text',			
							reference:'message',
							cls:'data-field',
							enforceMaxLength:true,
							maxLength:1000,
							minLength:4,
							allowBlank:false,
							width:600,
							height:200,
						}						
					]
				},
				{   id:'step-3',
					title:'Шаг 3. Номера телефонов',
					
					layout: {type:'hbox', align:'stretch', pack:'center'},
					items:[
						{	xtype:'container',
							height:300,
							width:180,
							layout: {type:'vbox', align:'center',pack:'center'},
							border:1,
							defaults:{margin:'5 5 5 5', width:150},
							items:[
								{	xtype:'container',
									maxHeight:50,
									cls:'description',
									html:'Введите телефоны'
								},
								{	xtype:'textareafield',
									name:'phones',
									cls:'data-field',									
									labelAlign:'top',
									minHeight:150,
									maxHeight:300,
									flex:1,
									maskRe:/^([0-9]{0,11})$/
								},
								{	xtype:'button',
									cls:'data-button',
									handler: 'btnAddPhone',
									text:'Добавить',
									scale:'large'
								},
								/*
								{	xtype:'button',
									cls:'data-button',
									handler:'btnStart',
									text:'Запустить рассылку',
									margin:'60 0 0 0'
								}*/
							]
						},
						{	xtype:'container',
							minHeight:250,
							width:180,
							layout: {type:'vbox', align:'stretch',pack:'center'},
							border:1,
							defaults:{margin:'5 5 5 5', width:200},
							items:[
								{	xtype:'container',
									maxHeight:50,
									cls:'description',
									html:'Получатели'
								},
								{	xtype: 'grid',
									reference: 'gridPhones',
									cls:'grid-panel',
									minHeight:250,
									maxHeight:400,
									border:1,
									flex:1,
									autoScroll:true,
									forceFit:true,
									store: new Ext.create('Ext.data.ArrayStore', {
										fields: [
											{name: 'phone', type: 'string'}
										]
									}),
									columns:[
										{ text: 'Телефон',  dataIndex: 'phone' },
										{	xtype:'actioncolumn',
											width:40,
											align:'center',
											items: [{
												icon: '/resources/images/delete.png',
												tooltip: 'Удалить',
												handler: 'btnDeletePhone'
											}]
										}
									]
								}
							]
						}
					]
				},
				{   id:'step-4',
					title:'Шаг 4. Описание рассылки',
					defaults:{margin:'5 5 5 5'},
					layout: {type:'vbox', align:'center', pack:'center'},					
					items:[
						{	xtype:'container',
							flex:1,
							maxWidth:500,
							maxHeight:30,
							cls:'description',
							html:'Укажите описание рассылки'
						},
						{	xtype:'textarea',
							name:'description',
							reference:'description',
							enforceMaxLength:true,
							cls:'data-field',
							maxLength:100,							
							minLength:4,
							allowBlank:false,
							width:500,
							height:40,
							disabled:true
						}						
					]					
				},
				{   id:'step-5',
					title:'Шаг 5. Запуск рассылки',
					layout: {type:'vbox', align:'center', pack:'center'},
					items:[
						{	xtype:'button', handler:'btnStart',text:'Запустить рассылку', scale:'large'}
					]
				}
			]			
		}	
	]
});

Ext.define('opers.view.AddChannel', { 	
	extend:'Ext.form.Panel',
	requires:['opers.controller.SendOneToAll'],
	controller: 'AddChannelController',
	floating:true,
	title:'Добавление канала',
	width:550,
	draggable:true,
	modal:true,
	border:1,
	height:200,
	bodyPadding:'15 15 15 15',
	layout:'anchor',
	defaults:{labelWidth:100, labelAlign:'right'},
	items:[
		{	xtype:'combo',
			store: 'channelsStore',
			name: 'channel',
			itemId:'channel',
			displayField:'channelDescription',
			valueField:'channel',
			anchor:'50%',
			qyeryMode:'remote',
			cls:'data-field',
			fieldLabel:'Канал',
			allowBlank:false,	
			editable:false		
		}
	],
	bbar:[
		{xtype:'tbfill' },
		{xtype:'button', handler:'btnAdd',text:'Добавить'},
		{xtype:'button', handler:'btnClose',text:'Закрыть'}
	]
});
