Ext.define('Product.New',{
	extend: 'Product.Form'
	,alias: ['widget.productnew']
	,constrainHeader: true
	,maximizable: true
	,buttons:[{
		text: '$view.save'
		,fixed: true
		,handler: function(btn,e){			
		    var ar = new ActiveRecord('Product');
			var fp = btn.up('form');
			var task = new Ext.util.DelayedTask(function(){
				var rs = ar.save();
				if(rs.status == 1){
					var win = btn.up('window');	
					win.remove(fp,false);
					win.add({xtype: 'productedit',postman:{ar: ar}});
					ar.write(win);
					win.setTitle('$view.edit ' + ar.data.id);					    
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
	}]
	,
	initComponent: function() { 
	    var me = this;
        me.callParent(arguments);
		var ar = new ActiveRecord('Product');
		ar.setValidates(me,$Product.config);		
   }
});