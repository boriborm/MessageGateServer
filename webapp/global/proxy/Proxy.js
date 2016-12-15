Ext.define('global.proxy.Proxy', {
	alternateClassName: 'JwgProxy',
    extend: 'Ext.data.proxy.Ajax',
	alias:'proxy.jwg_proxy',
    url: '/json/',
	type: 'ajax',
	timeout: 60000,
	actionMethods: {update: 'POST', read:'GET', create:'POST', destroy:'POST'},
	loadMask:true,
	processResult:{ 
		status: undefined,
		statusText: undefined
	}
});
