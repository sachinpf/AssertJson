## Json Deep Equal

#### A. Common Use Cases:
> 1. Compare two JsonArray objects
> 2. Store comparision results
***     

#### B. Expected Functionality:
> 1) Compare two JsonArray objects by locating JsonObjects using ID
> 2) Summarize comparision outcome as below: 
    <br> - Missing JsonObjects from both the JsonArrays 
    <br> - Missing Keys from each matching JsonObject 
    <br> - Not matching key/values for each JsonObject from both JsonArrays
> 3) Ability to configure comparision as below:
     <br> - Ignore certain fields
     <br> - Ignore case (upper/lower)
     <br> - Recognize 0,1,Y,N as boolean based on other object values
     <br> - Recognize 50.00 and 50 as equal
     <br> - Trim Strings 
     <br> - Recognize variety of date formats like 11 April 2010 equals 04/11/2010
     <br> - Recognize different time formats
     <br> - Ignore seconds/milli seconds
     <br> - 
     <br> - 
     <br> -  
***