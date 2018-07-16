/**
 * Version 1.0 Support Extjs4 
 * @param arg0 - Model name
 * @param arg1 - id
 */

function ActiveRecord() {
	
	this.constructor = function(args){
		//Model Name for args 0
		this.name = args[0];
		switch(args.length){
		    case 2:
		    	//id value for args 1
				this.data.id = args[1];
				var result = ActiveRecord.first(this.name,this.data.id);
				if(result.status === 1){
					this.status = result.status;
					this.data = result.data;
				}else{
					this.status = result.status;
					this.msg = result.msg;
					this.data = null;
				}				
				break;
			default:
				break;
		}
	},	
	this.refresh = function(){
		if(this.data){
			if(arguments[0]){
				this.refreshAt = 0;
			}
			var currentAt = new Date().getTime();				
			if((currentAt - this.refreshAt) > 3000){
				var result = ActiveRecord.first(this.name,this.data.id);
				if(result.status === 1){
					this.refreshAt = currentAt;
					this.status = result.status;				
					this.data = result.data;				
				}else{
					this.refreshAt = 0;
					this.status = result.status;
					this.msg = result.msg
					this.data = null;
				}				
			}
		}		
	},
	this.belongsTo = function(belongName){
		if(this.belongs[belongName]){
			return this.belongs[belongName];
		}else{
			var ar = new ActiveRecord(this.name);
			ar.data = this.data;
			ar.params.belongName = belongName;
			ar.submitParams = true;
			var rs = ar.submit('belongsTo');
			if(rs.status === 1){
				var belongAr = new ActiveRecord(belongName);
				belongAr.data = rs.data;
				belongAr.name = rs.name;
				this.belongs[belongName] = belongAr;
			}else
				this.belongs[belongName] = null;
		}
		return this.belongs[belongName];
	},
	this.hasOne = function(hasName){
		if(this.has[hasName]){
			return this.has[hasName];
		}else{
			var ar = new ActiveRecord(this.name);
			ar.data = this.data;
			ar.params.hasName = hasName;
			ar.submitParams = true;
			var rs = ar.submit('hasOne');
			if(rs.status === 1){
				var hasAr = new ActiveRecord(hasName);
				hasAr.data = rs.data;
				hasAr.name = rs.name;
				this.has[hasName] = hasAr;
			}else
				this.has[hasName] = null;
		}
		return this.has[hasName];
	},
	this.hasMany = function(hasName){
		if(this.has[hasName]){
			return this.has[hasName];
		}else{
			var ar = new ActiveRecord(this.name);
			ar.data = this.data;
			ar.params.hasName = hasName;
			ar.submitParams = true;			
			this.has[hasName] = ar.submit('hasMany');
		}
		return this.has[hasName];
	},
	//Write this.data to form
	this.write = function(form,funs){
		this.form = form;
		var b = true;
		funs = (funs === undefined ? {} : funs);
		if(funs.before)
			b = funs.before(this);
		b = (b === undefined ? true : b);
		if(b)
			ActiveRecord.write(this,funs.after,funs.execute);		
	},
	//Read form data to this.data
	this.read = function(form,funs){
		this.form = form;
		var b = true;
		funs = (funs === undefined ? {} : funs);
		if(funs.before)
			b = funs.before(this);
		b = (b === undefined ? true : b);
		if(b)
			ActiveRecord.read(this,funs.after,funs.execute);
	},
	this.submit = function(action){
		var result = null;
		Ext.Ajax.request({
			type: 'POST',
			async: false,
			url: ActiveRecord.rule(this,action),
			params: ActiveRecord.merge(this.submitParams,this.name,this.data,this.params),
			success: function(response){
				 result = eval('(' + response.responseText + ')');
			}
		});
		 if(result.status === 1){
			 this.status = result.status;
			 this.data = result.data;
		 }else{
			 this.status = result.status;
			 this.msg = result.msg;
		 }			 
		return result;
	},
	//Create this.data to remote
	this.create = function(){
		return this.submit('create');
	},
	//Update this.data to remote
	this.update = function(){
		return this.submit('update');
	},
	//Create or Update (id is null to create else to update) 
	this.save = function(){
		if(this.data.id === undefined || this.data.id === null || this.data.id === '')
			return this.create();
		else
			return this.update();
	},
	//Remove to remote (this.data.id is necessity)
	this.remove = function(){
		return this.submit("remove");
	},
	this.setValidates = function(form,attrs){
		var me = this;
		me.form = form;
		me.read(form);
		var inputs = form.query('field');
		Ext.Array.each(inputs,function(input){
			var name = input.name;
			if(ActiveRecord.NameRegExp(me.name).test(name)){
				var attr = ActiveRecord.ParseAttr(me.name,name);				
				var cnf = attrs[attr] || {};
					
				if(cnf['default']){
					if(me.form.xtype === me.name.toLowerCase() + 'new')
						input.setValue(cnf['default']);
				}
				
				if(cnf.validates_presence_of !== undefined)	
					ActiveRecord.validatesPresence(cnf.validates_presence_of || {},me,attr,input);
				
				if(cnf.validates_format_of !== undefined)					
					ActiveRecord.validatesFormat(cnf.validates_format_of || {},me,attr,input);
				
				if(cnf.validates_length_of !== undefined && cnf.validates_format_of === undefined)
					ActiveRecord.validatesLength(cnf.validates_length_of || {},me,attr,input);				
				
				if(cnf.validates_number_of !== undefined)
					ActiveRecord.validatesNumber(cnf.validates_number_of || {},me,attr,input);
				
				if(cnf.validates_timestamp_of !== undefined)
					ActiveRecord.validatesTimestamp(cnf.validates_timestamp_of || {},me,attr,input);
				
				if(cnf.validates_date_of !== undefined)
					ActiveRecord.validatesDate(cnf.validates_date_of || {},me,attr,input);
				
				if(cnf.validates_time_of !== undefined)
					ActiveRecord.validatesTime(cnf.validates_time_of || {},me,attr,input);
				
				if(cnf.validates_uniqueness_of !== undefined)
					ActiveRecord.validatesUniqueness(cnf.validates_uniqueness_of || {},me,attr,input); 
				
			}
		});
	},
	this.submitParams = false,
	this.name,
	this.form,
	this.status = 1,
	this.params = {},
	this.data = {},
	this.belongs = {},
	this.has = {},
	this.refreshAt = 0;
	return this.constructor(arguments);
}

ActiveRecord.validatesPresence = function(validCnf,me,attr,input){
	if(!ActiveRecord.onValid(validCnf.on,me))
		return;
	
	input.allowBlank = false;
	input.blankText = validCnf.messages.message;
}

ActiveRecord.validatesFormat = function(validCnf,me,attr,input){
	if(!ActiveRecord.onValid(validCnf.on,me))
		return;
	
	input.regex = new RegExp(validCnf['with']);
	input.regexText = validCnf.messages['with'];
}

ActiveRecord.validatesNumber = function(validCnf,me,attr,input){
	if(!ActiveRecord.onValid(validCnf.on,me))
		return;
	
	if(validCnf.greater_than_or_equal_to){
		input.minValue = validCnf.greater_than_or_equal_to
		input.minText = validCnf.messages.greater_than_or_equal_to
	}
	if(validCnf.less_than_or_equal_to){
		input.maxValue = validCnf.less_than_or_equal_to
		input.maxText = validCnf.messages.less_than_or_equal_to
	}
	if(validCnf.within){
		input.minValue = validCnf.within[0];
		input.maxValue = validCnf.within[1];
		input.minText = validCnf.messages.within;
		input.maxText = validCnf.messages.within;
	}
	if(validCnf.equal_to){
		input.setValue(validCnf.equal_to);
		input.setReadOnly(true);
	}
}

ActiveRecord.validatesLength = function(validCnf,me,attr,input){
	if(!ActiveRecord.onValid(validCnf.on,me))
		return;
		
	if(validCnf.minimum){
		input.minLength = validCnf.minimum;
		input.minLengthText = validCnf.messages.minimum;
	}
	if(validCnf.maximum){
		input.maxLength = validCnf.maximum;
	    input.maxLengthText = validCnf.messages.maximum;
	}
	if(validCnf.is){
		input.setValue(validCnf.is);
		input.setReadOnly(true);
	}
	if(validCnf.within){
		input.minLength = validCnf.within[0];
		input.maxLength = validCnf.within[1];
		input.minLengthText = validCnf.messages.within;
		input.maxLengthText = validCnf.messages.within;
	}
}

ActiveRecord.validatesTimestamp = function(validCnf,me,attr,input){
	if(!ActiveRecord.onValid(validCnf.on,me))
		return;
	
	if(validCnf.minimum)
		input.minValue = Formats.text2date(validCnf.minimum);
	if(validCnf.maximum)
		input.maxValue = Formats.text2date(validCnf.maximum);
	if(validCnf.within){
		input.minValue = Formats.text2date(validCnf.within[0]);
		input.maxValue = Formats.text2date(validCnf.within[1]);
	}
	if(validCnf.within){
		input.minValue = Formats.text2date(validCnf.within[0]);
		input.maxValue = Formats.text2date(validCnf.within[1]);
		input.minText = validCnf.messages.within;
		input.maxText = validCnf.messages.within;
	}
	if(validCnf.is){
		input.setValue(validCnf.is);
		input.setReadOnly(true);
	}
}

ActiveRecord.validatesDate = function(validCnf,me,attr,input){
	if(!ActiveRecord.onValid(validCnf.on,me))
		return;
	
	if(validCnf.minimum){
		input.minValue = Formats.text2date(validCnf.minimum);
		input.minText = validCnf.messages.minimum
	}
	if(validCnf.maximum){
		input.maxValue = Formats.text2date(validCnf.maximum);
		input.maxText = validCnf.messages.maximum
	}
	if(validCnf.within){
		input.minValue = Formats.text2date(validCnf.within[0]);
		input.maxValue = Formats.text2date(validCnf.within[1]);
		input.minText = validCnf.messages.minimum
		input.maxText = validCnf.messages.maximum
	}
	if(validCnf.is){
		input.setValue(validCnf.is);
		input.setReadOnly(true);
	}
}

ActiveRecord.validatesTime = function(validCnf,me,attr,input){
	if(!ActiveRecord.onValid(validCnf.on,me))
		return;
	
	if(validCnf.minimum)
		input.minValue = Formats.text2time(validCnf.minimum);
	if(validCnf.maximum)
		input.maxValue = Formats.text2time(validCnf.maximum);
	if(validCnf.within){
		input.minValue = Formats.text2time(validCnf.within[0]);
		input.maxValue = Formats.text2time(validCnf.within[1]);
	}
	if(validCnf.within){
		input.minValue = Formats.text2time(validCnf.within[0]);
		input.maxValue = Formats.text2time(validCnf.within[1]);
		input.minText = validCnf.messages.minimum
		input.maxText = validCnf.messages.maximum
	}
	if(validCnf.is){
		input.setValue(validCnf.is);
		input.setReadOnly(true);
	}
}

ActiveRecord.validatesUniqueness = function(validCnf,me,attr,input){	
	if(!ActiveRecord.onValid(validCnf.on,me))
		return;

	input.onFocus = function(e,opt){
		input.validator = function(v){
			 	return true;
		}
		input.isValid();
    }
	input.onBlur = function(e,opt){
		 if(!input.isValid())
			 return true;
		 
		 me.read(me.form);
		 var ajax = new Ajax(me.name + '/uniquenessValidate/' + ActiveRecord.args,{
			 field: attr,
			 validates: Ext.encode(me.data)
		 });		 
		 ajax.method = 'POST';
		 var rs = ajax.getObject();		 
		 if(rs.status === 2){
			 input.validator = function(v){
				 return rs.msg;
			 }
			 input.isValid();
		 }
	}
}

ActiveRecord.onValid = function(on,me){
	var on = on || 'save';
	var newXtype = me.name.toLowerCase() + 'new';
	var editXtype = me.name.toLowerCase() + 'edit';
	if(on === 'create' && newXtype !== me.form.xtype)
		return false;
	
	if(on === 'update' && editXtype !== me.form.xtype)
		return false;
	
	return true;
}

ActiveRecord.merge = function(merged,name,data,params){
	var allParams = {};
	if(merged){			
		for(var k in params)
			allParams[k] = params[k];
	}
	for(var k in data)
		allParams[name + '[' + k + ']'] = data[k];
		
	return allParams;
}

ActiveRecord.read = function(me,after,execute){
	var inputs = me.form.query("field");
	for(var index in inputs){
		var input = inputs[index];
		var name = input.name;
		var attr = ActiveRecord.ParseAttr(me.name,name);
		var value;	
		var execNext = true;
		if(input.xtype === 'radiofield' || input.xtype === 'radio'){
			var iv = input.inputValue || '';
			if(iv != ''){
				if(input.checked)
					value = iv;
				else
					continue;
			}else{
				value = input.checked;
			}		
		}else if(input.xtype === 'checkboxfield' || input.xtype === 'checkbox'){
			var iv = input.inputValue || '';
			if(iv != '' && iv != 'on'){
				var vs = {};
				vs[iv] = input.checked;
				value = Ext.encode(vs);
			}else
				value = input.checked;
		}else if(input.xtype === 'datetimefield' 
			     || input.xtype === 'datefield' 
			     || input.xtype === 'timefield'){
		   	value = Ext.Date.format(input.getValue(),input.format);
		}else{
			value = input.getValue();
		}
		if(execute)
			execNext = execute(me,inputs,input,name,attr,value);
		
	    if(execNext){
			if(ActiveRecord.NameRegExp(me.name).test(name)){				
				me.data[attr] = value;
			}else{				
				var arr = [];
				if(me.params[name] === undefined){
					me.params[name] = value;
				}else{
					if(Array.isArray(me.params[name])){
						arr = me.params[name];
					}else{
						arr = [me.params[name]];
					}
					arr = arr.concat(value);
					me.params[name] = arr;
				}
			}
	    }
	}
	if(after)
		after(me,inputs);
}

ActiveRecord.write = function(me,after,execute){
	var inputs = me.form.query("field");
    Ext.Array.each(inputs,function(input){
    	var name = input.name;
    	var attr;
    	var value;
    	var isAtt = ActiveRecord.NameRegExp(me.name).test(name);
    	var values = me.data || {};
    	var execNext = true;
    	if(isAtt){
    		attr = ActiveRecord.ParseAttr(me.name,name);
    		value = values[attr];
    	}else{
    		value = values[name];
    	}    	
    	if(execute)
    		execNext = execute(me,inputs,input,name,attr,value);
    	   	
    	if(execNext){
    		if(isAtt){
    			if(typeof(value) === 'number' || typeof(value) === 'boolean')
    				input.setValue(value);
    			else
    				input.setValue(value || '');
    		}    			
    	}
    });    
    if(after)
		after(me,inputs);
}

ActiveRecord.NameRegExp = function(model){
	return new RegExp('^'+model+'\\[[_\\w\\d]+\\]$','g');
}

ActiveRecord.ParseAttr = function(model,name){
	return name.replace(new RegExp('^' + model + '\\[','g'),'')
			 .replace(new RegExp('\\]$','g'),'');
}

ActiveRecord.first = function(name,id){
	var q = new Query(name);
	q.and('eq_id',id);
	return q.first();
}

ActiveRecord.rule = function(me,action){
	return ActiveRecord.url + '/' + me.name + '/' + action + '/' + ActiveRecord.args;
}
