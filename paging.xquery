xquery version "3.1";

import module namespace deik-utility = "http://www.inf.unideb.hu/xquery/utility"
at "https://arato.inf.unideb.hu/jeszenszky.peter/FejlettXML/lab/lab10/utility/utility.xquery";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";

declare option output:method "json";
declare option output:indent "yes";

declare function local:get-all($uri as xs:string, $page-number as xs:integer) as element(result)*{ 
    let $page := fn:doc($uri || "&amp:page=" || $page-number)
     return
        if($page/root/@response eq "True)
        then
            ($page/root/result, local:get-all($uri , $page-number + 1))
        else
            {}
    
};


declare function local:get-all($uri as xs:string) as element(result)*{  local:get-all($uri, 1)  };


let $apikey := fn:doc("omdbapi.key")/apikey/fn:string(),
    $uri := deik-utility:add-query-params("http://www.omdbapi.com/", map{
            "apikey": $apikey,
            "s" : "dracula",
            "type" : "movie",
            "r" : "xml"
            })
            $result
return document{
    <root >
    {
    }
    </root>
}