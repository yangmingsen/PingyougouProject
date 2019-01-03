var app = angular.module('pingyougou',[]);

/*$sce服务写成过滤器*/
app.filter('trustHtml',['$sce',function($sce){
    return function(data){//传入过滤的内容
        return $sce.trustAsHtml(data);//返回的过滤后的内容（信任的html）
    }
}]);
