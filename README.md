# SOAPTASK

- WSDL url üzerinden erişmek yerine dosya olarak resources/wsdl içine kopyalandı. Offline olarak çalışabilmesi ve buildin dış servisin erişilebilirliğine bağlı olmaması ve tekrarlanabilir olması için.
- countryCode ve vatNumber sadece xsd:string olarak tanımlanmış,
dokümanda ; The countryCode input parameter must follow the pattern [A-Z]{2} ve The vatNumber input parameter must follow the pattern [0-9A-Za-z\+\*\.]{2,12} 
cümleleri ile belirtilse de, xsd:string olarak tanımlandığı için pattern validation yapılmamaktadır. Bu yüzden format kontrolünü validation ile yaptım.

-WSDL dosyasındaki <wsdlsoap:address> değeri http:// şemasında. 
-validation paternleri dokümanda belirtilen patternler ile aynı şekilde yazıldı.