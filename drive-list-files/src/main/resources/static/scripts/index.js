$(document).ready(function(){
	$("#searchbar").change(function(e){
        var query = e.target.value;
        $.ajax({

            url : 'http://localhost:8090/search/files?q='+query,
            type : 'GET',
            dataType:'json',
            success : function(data) {
                console.log(data);
                $("#searchcontent").html(data.map((val)=>'<li><span><h2>id: '+val.id+'</h4></span><span> <h4>name: '+val.fileName+'</h4></span><span>link: <a href='+val.webContentLink+'/>'+val.webContentLink+'</span></li>'));
            },
            error : function(request,error)
            {
                alert("Request: "+JSON.stringify(request));
            }
        });
	});
});