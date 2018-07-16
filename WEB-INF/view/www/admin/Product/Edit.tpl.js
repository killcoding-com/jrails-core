Ext.define('Product.Edit',{
	extend: 'Product.Form'
	,alias: ['widget.productedit']
	,buttons:[{
		text: '$view.save'
		,fixed: true
		,handler: function(btn,e){			
		    var ar = new ActiveRecord('Product');
		    var fp = btn.up('form');
			var task = new Ext.util.DelayedTask(function(){
				var rs = ar.save();
				if(rs.status == 1){
					var fp = btn.up('form');
					ar.write(fp);
					btn.up('window').setTitle('$view.edit ' + ar.data.id);					    
					Locale.success();
				}else{
					Locale.failure(rs.status,rs.msg);
				}
				btn.enable();
				btn.setText('$view.save');
				var store = Ext.data.StoreManager.lookup('Product-Store');
				if(store){
				    store.reload();
				}
			});			
			new Ext.util.DelayedTask(function(){				
				var form = fp.getForm();
				if(form.isValid()){
					ar.read(fp);
					btn.setText('$view.saveing');
					btn.disable();					
					task.delay(200);
				}				
			}).delay(1);					
		}
	},{
		xtype:'button'
		,text: '$view.remove'
		,dock: 'bottom'
		,fixed: true
		,handler: function(btn,e){
			Locale.rmConfirm(function(){
				var ar = new ActiveRecord('Product');
				var task = new Ext.util.DelayedTask(function(){
					var rs = ar.remove();
  					if(rs.status == 1)
  						Locale.success();
  					else
  						Locale.failure(rs.status,rs.msg);
  					
  					btn.enable();
  					btn.setText('$view.remove');	
  					btn.up('window').close();
					var store = Ext.data.StoreManager.lookup('Product-Store');
					if(store){
				        store.reload();
				    }
				});	
				new Ext.util.DelayedTask(function(){
					btn.disable();
					btn.setText('$view.removing');					
					ar.read(btn.up('form'));					
  					task.delay(200);
				}).delay(1);
			});
		}
	}]
	,
	initComponent: function() { 
	    var me = this;
        me.callParent(arguments);
        me.postman.ar.write(this);
		me.postman.ar.setValidates(this,$Product.config);		
   }
});