
var Locale = {

}

//数据处理成功
Locale.success = function(msg){
	new Rails.Tip().msg('<b>恭喜</b>', msg || '操作已完成。');
}

//数据处理失败
Locale.failure = function(status,msg){
	if(arguments){
		new Rails.Tip().msg('<b>失败</b>','错误('+status+'),' + msg);
	}else{
		new Rails.Tip().msg('<b>失败</b>','操作失败，请重试！');
	}
}

//无选择数据时提示
Locale.noSelected = function(msg){
	new Rails.Tip().msg('<b>失败</b>', msg || '你没有选择数据!');
}

//查询完成
Locale.searchSuccess = function(msg){
	new Rails.Tip().msg('<b>恭喜</b>', msg || '查询已完成。');
}

//查询失败
Locale.searchFailure = function(msg){
	new Rails.Tip().msg('<b>失败</b>', msg || '查询失败，请重试！');
}

Locale.rmConfirm = function(yes,no){
	Ext.Msg.confirm('删除?','确定删除这数据吗?',
			function(btn, text) {
				if (btn == 'yes') {
					if(yes)
						yes();
				}else if (btn == 'no'){
					if(no)
						no();
				}
			}
	);
}

Locale.mask = function(){
	new Ext.util.DelayedTask(function(){
		Ext.getBody().mask('请等待...');
	}).delay(20);
}

