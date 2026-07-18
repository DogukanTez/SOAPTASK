# SOAPTASK

- WSDL url üzerinden erişmek yerine dosya olarak resources/wsdl içine kopyalandı. Offline olarak çalışabilmesi ve buildin dış servisin erişilebilirliğine bağlı olmaması ve tekrarlanabilir olması için.
- countryCode ve vatNumber sadece xsd:string olarak tanımlanmış,
dokümanda ; The countryCode input parameter must follow the pattern [A-Z]{2} ve The vatNumber input parameter must follow the pattern [0-9A-Za-z\+\*\.]{2,12} 
cümleleri ile belirtilse de, xsd:string olarak tanımlandığı için pattern validation yapılmamaktadır. Bu yüzden format kontrolünü validation ile yaptım.

- WSDL dosyasındaki <wsdlsoap:address> değeri http:// şemasında. 
- Validation paternleri dokümanda belirtilen patternler ile aynı şekilde yazıldı.

- Daha önce wsdl, soap ve xsd dosyaları ile çalışmadığım için tasarım kısmında calude ve chatgpt den yardım aldım. Buna bağlı olarak xsd dosyası mevcutta olmadığından kendim oluşturmayı ve onun üzerinden gitmeyi tercih ettim.
- resources altına xsd klasörünü oluşturdum

- Exception classlarının amaçları şöyle: 
- TemporaryException tekrar denenmek üzere,
- TechnicalException tekrar denenmeyecek, loglanacak ve kullanıcıya hata mesajı dönecek,
- InvalidInputException validation hatası için, kullanıcıya hata mesajı dönecek.

- ViesFaultCode içinde contains kullanılmasının sebebi esneklik sağlanması.VIES bazen kodu düz döndürüyor, bazen uzun açıklama metninin içine gömebiliyormuş. Bu nedenle tercih ettim.

- Her iki KDV numarası da (alıcı,satıcı) her durumda sorgulanıyor. Satıcı numarası geçersiz çıksa bile alıcı sorgusu atlanmıyor. Kullanıcı tek istekte her iki tarafın durumunu da görüyor, hatalı fatura için iki kez denemek zorunda kalmaması için.
- Valid false olsa bile bu bir hata değildir, iş sonucudur. Exception verilemden devam ediliyor. 
- Akışta exception fırlatılmadan devam ediliyor ve NOT_ISSUABLE olarak işaretleniyor. Bu ayrım ViexClient içinde yapılıyor. InvoiceValidationService içinde yalnızca iş kuralları var.

-Mapper paketi açtım ancak mapstruct kütüphanesi kullanmadım. Ekstra dependency eklememek için basit şekilde oluşturdum.