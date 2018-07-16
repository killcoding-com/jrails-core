

function Ajax(){

	this.constructor = function(args){
		this.url = Ajax.url + '/' + args[0];
		this.params = args[1] || {};
	},	
	this.getObject = function(){
		return eval('(' + this.request() + ')');
	},
	this.run = function(){
		try{
			eval(this.request());
		}catch(e){
			alert('Ajax run error : ' + e.message);
		}
	},
	this.request = function(){
		var result;
		Ext.Ajax.request({
		    url: this.url,
		    async: false,
		    method: this.method,
		    params: Ext.Object.toQueryString(this.params,false),
		    success: function(response){
		        result = response.responseText;
		    }
		});
		return result;
	},
	this.method = 'GET';
	return this.constructor(arguments);
}

