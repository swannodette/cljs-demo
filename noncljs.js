window.next = function(){
        var pres = document.getElementsByTagName("pre");
        pres[0].style.display = "none";
        pres[1].style.display = "block";
        var strongs = document.getElementsByTagName("strong")
        for(var i=0,len=strongs.length;i<len;i++){
           var strong = strongs[i];
           strong.style.display = "none";
        }
}
