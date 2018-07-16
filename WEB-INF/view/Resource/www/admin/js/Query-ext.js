/** 
 * Version 1.0
 */
function Query() {

	this.constructor = function(args){
		this.from = args[0],
		this.ands = {},
		this.ors = {}, 
		this.selects = [],
		this.alias = [],
		this.lefts = [],			
		this.rights = [], 
		this.inners = [], 
		this.counts = [],			
		this.avgs = [], 
		this.mins = [], 
		this.maxs = [], 
		this.sums = [],	
		this.params = {}, //request params
		this.otherParams = {},
		this.autoSkipnil = Query.False, 
		this.autoJoin = Query.False, 
		this.autoGroup = Query.False,			
		this.autoSelect = Query.False,
		this._limit = null,
		this._offset = null;
	},	
	this.join = function(auto) {
				this.autoJoin = auto || Query.False;
				return this;
			}, 
	this.skipnil = function(skipnil) {
				this.autoSkipnil = skipnil || Query.False;
				return this;
			}, 
	this.group = function(auto) {
				this.autoGroup = auto || Query.False;
				return this;
			}, 
	this.select = function() {
				switch(arguments.length){
					case 1:
						if(typeof(arguments[0]) == 'boolean'){
							this.autoSelect = arguments[0];
						}else{
							this.selects.push([arguments[0]]);
						}
						break;
					case 2:
						this.selects.push([arguments[0],arguments[1]]);	
						break;
					case 3:
						this.selects.push([arguments[0],arguments[1],arguments[2]]);	
						break;
					default:
						break;
				}
				return this;
			}
	this.as = function() {
				switch(arguments.length){
					case 1:
						this.alias.push([arguments[0]]);
					case 2:
						this.alias.push([arguments[0],arguments[1]]);	
						break;
					case 3:
						this.alias.push([arguments[0],arguments[1],arguments[2]]);	
						break;
					default:
						break;
				}
				return this;
	}
	this.and = function() {
		switch(arguments.length){
			case 1:
				if(typeof(arguments[0]) === 'object'){
					this.ands = arguments[0];
				}else{
					this.ands[arguments[0]] = Query.True;	
				}							
				break;
			case 2:
				if(arguments[1] == null)
					this.ands[arguments[0]] = '';
			    else if(typeof(arguments[1]) == 'boolean')
					this.ands[arguments[0]] = arguments[1].toString();
				else
					this.ands[arguments[0]] = arguments[1];
				break;
			default:
				break;
		}
		return this;
	}, this.or = function() {
		switch(arguments.length){
		case 1:
			if(typeof(arguments[0]) === 'object'){
				this.ors = arguments[0];
			}else{
				this.ors[arguments[0]] = Query.True;	
			}									
			break;
		case 2:
			if(arguments[1] == null)
				this.ors[arguments[0]] = '';
		    else if(typeof(arguments[1]) == 'boolean')
				this.ors[arguments[0]] = arguments[1].toString();
			else
				this.ors[arguments[0]] = arguments[1];
			break;
		default:
			break;
	}
		return this;
	}
	,this.order = function(){
		switch(arguments.length){
			case 2:
				this.and(arguments[1] + '_' + arguments[0]);
				break;
			case 3:
				this.and(arguments[2] + '_' + arguments[1] + '_from_' + arguments[0]);
				break;
			default:
				break;
		}	
		return this;
	}
	,this.asc = function(){
		switch(arguments.length){
			case 1:
				this.order(arguments[0],'asc');
				break;
			case 2:
				this.order(arguments[0],arguments[1],'asc');
				break;
			default:
				break;
		}	
		return this;
	}
	,this.desc = function(){
		switch(arguments.length){
			case 1:
				this.order(arguments[0],'desc');
				break;
			case 2:
				this.order(arguments[0],arguments[1],'desc');
				break;
			default:
				break;
		}	
		return this;
	}
	,this.inner = function(t, fk) {
		if (fk)
			this.inners.push([t,fk]);
		else
			this.inners.push([t]);
		return this;
	}, this.left = function(t, fk) {
		if (fk)
			this.lefts.push([t,fk]);
		else
			this.lefts.push([t]);
		return this;
	}, this.right = function(t, fk) {
		if (fk)
			this.rights.push([t,fk]);
		else
			this.rights.push([t]);
		return this;
	}, this.count = function(t, c) {
		this.counts.push([t,c]);
		return this;
	}, this.sum = function(t, c) {
		this.sums.push([t,c]);
		return this;
	}, this.avg = function(t, c) {
		this.avgs.push([t,c]);
		return this;
	}, this.min = function(t, c) {
		this.mins.push([t,c]);
		return this;
	}, this.max = function(t, c) {
		this.maxs.push([t,c]);
		return this;
	}, this.find = function(){
		this.args(arguments);
		return this.request('query','find');
	},this.first = function(){
		this.args(arguments);
		return this.request('query','first');
	},this.query = function(){
		switch(arguments.length){
			case 1:
				break;
			case 2:
				this.args(arguments[1]);
				break;
			case 3:
				this.args(arguments[1],arguments[2]);
				break;
			case 4:
				this.args(arguments[1],arguments[2],arguments[3]);
				break;
			default:
				break;
		}		
		return this.request('query',arguments[0]);
	},this.limit = function(limit){
		this._limit = limit;
		return this;
	},this.offset = function(offset){
		this._offset = offset;
		return this;
	},
	this.args = function(args){
		switch(args.length){
		case 0:
			this.params['disc'] = Query.False;
			this.params['first'] = '';
			this.params['last'] = '';			
			break;
		case 1:
			this.params['disc'] = args[0];
			this.params['first'] = '';
			this.params['last'] = args[0];		
			break;
		case 2:
			this.params['disc'] = Query.False;
			this.params['first'] = args[0];
			this.params['last'] = args[1];	
			break;
		case 3:
			this.params['disc'] = args[0];
			this.params['first'] = args[1];
			this.params['last'] = args[2];
			break;
		default:	
			this.params['disc'] = args[0];
			this.params['first'] = args[1];
			this.params['last'] = args[2];
			break;
		}
	},this.request = function(action,fun){
		var result = null;
		this.params['fun'] = fun;
		this.params['join'] = this.autoJoin.toString();
		this.params['group'] = this.autoGroup.toString();
		this.params['select'] = this.autoSelect.toString();
		this.params['skipnil'] = this.autoSkipnil.toString();
		this.params['ands'] = Query.encode(this.ands);
		this.params['ors'] = Query.encode(this.ors);
		this.params['selects'] = Query.encode(this.selects);
		this.params['alias'] = Query.encode(this.alias);
		this.params['lefts'] = Query.encode(this.lefts);
		this.params['rights'] = Query.encode(this.rights);
		this.params['inners'] = Query.encode(this.inners);
		this.params['counts'] = Query.encode(this.counts);
		this.params['avgs'] = Query.encode(this.avgs);
		this.params['mins'] = Query.encode(this.mins);
		this.params['maxs'] = Query.encode(this.maxs);
		this.params['sums'] = Query.encode(this.sums);
		this.params['otherParams'] = Query.encode(this.otherParams);
		if(this._offset !== null)
			this.params['offset'] = this._offset;
		
		if(this._limit !== null)
			this.params['limit'] = this._limit;		
		
		Ext.Ajax.request({
			type: 'POST',
			async: false,
			timeout: 30,
			url: Query.rule(this,this.from,action),
			params: Ext.Object.toQueryString(this.params,true),
			success: function(response,opts){
				 result = eval('(' + response.responseText + ')');
			}
		    ,
			failure: function(response, opts) {
			     result = {status: response.status,msg: 'Error : ' + response.status + ',' + response.statusText};
			}
		});
		return result;
	},	
	this.clone = function() {
		var copy = new Query(this.from);
		for(var d in this)
			copy[d] = this[d];	
		
		return copy;
	},
	this.destroy = function () {
		for(var d in this)
			delete this[d];		
	};
	return this.constructor(arguments);
};

Query.True = 'true';
Query.False = 'false';

Query.rule = function(me,from,action){
	return ActiveRecord.url + '/' + from + '/' + action + '/' + Query.args; 
}
Query.encode = function(o){
	return Ext.encode(o);
}
