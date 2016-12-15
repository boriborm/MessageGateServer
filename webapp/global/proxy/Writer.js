Ext.define('global.proxy.Writer', {
    extend: 'Ext.data.writer.Json',
    alternateClassName: 'JwgWriter',
	alias:'writer.jwg_json',
	writeRecords: function(request, data) {
        var root = this.root;
        if (this.expandData) {
            data = this.getExpandedData(data);
        }
        if (this.allowSingle && data.length === 1) {
            data = data[0];
        }
        if (this.encode) {
            if (root) {
                request.params[root] = Ext.encode(data);
            } else {
                Ext.Error.raise('Must specify a root when using encode');
            }
        } else {
            request.jsonData = request.jsonData || {};
            if (root) {
                request.jsonData[root] = data;
            } else {
                request.jsonData = data;
            }
        }
		if (request.jsonData instanceof Array)	for (j in request.jsonData)	Ext.applyIf(request.jsonData[j],request.params);
		else Ext.applyIf(request.jsonData,request.params);
		delete request.params;
		
        return request;
    }
});
