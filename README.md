# SOAPTASK

## Build ve çalıştırma

```bash
./mvnw clean install     # build + testler
./mvnw test              # sadece testler
./mvnw spring-boot:run   # uygulamayı başlat (port 8080)
```

## Örnek istek

```bash
curl -X POST http://localhost:8080/api/v1/invoice/validate \
  -H "Content-Type: application/json" \
  -d '{
    "invoiceNumber": "INV-001",
    "sellerCountryCode": "DE",
    "sellerVatNumber": "129273398",
    "buyerCountryCode": "DE",
    "buyerVatNumber": "129273398"
  }'
```

Cevap:

```json
{
  "invoiceNumber": "INV-001",
  "decision": "ISSUABLE",
  "reason": null,
  "seller": { "valid": true, "countryCode": "DE", "vatNumber": "129273398", ... },
  "buyer":  { "valid": true, "countryCode": "DE", "vatNumber": "129273398", ... }
}
```


## Tasarım notları ve iş kuralları

- WSDL url üzerinden erişmek yerine dosya olarak resources/wsdl içine kopyalandı. Offline olarak çalışabilmesi ve buildin dış servisin erişilebilirliğine bağlı olmaması ve tekrarlanabilir olması için.
- Dezavantajı servis tarafında sözleşme değişirse dosyanın manuel güncellenmesi gerekmesi.
- countryCode ve vatNumber sadece xsd:string olarak tanımlanmış,
dokümanda ; The countryCode input parameter must follow the pattern [A-Z]{2} ve The vatNumber input parameter must follow the pattern [0-9A-Za-z\+\*\.]{2,12} 
cümleleri ile belirtilse de, xsd:string olarak tanımlandığı için pattern validation yapılmamaktadır. Bu yüzden format kontrolünü validation ile yaptım.

- WSDL dosyasındaki <wsdlsoap:address> değeri http:// şemasında. 
- Validation paternleri dokümanda belirtilen patternler ile aynı şekilde yazıldı.
- Bu kontrol INVALID_INPUT hatasını yönetme ihtiyacını ortadan kaldırmıyor,
  `XX` gibi formatı geçerli ama tanımsız bir ülke kodu validation'dan geçer,
  servis tarafından reddedilir.

- Daha önce wsdl, soap ve xsd dosyaları ile çalışmadığım için tasarım kısmında claude ve chatgpt den yardım aldım. Buna bağlı olarak xsd dosyası mevcutta olmadığından kendim oluşturmayı ve onun üzerinden gitmeyi tercih ettim.
- VIES şeması bağımsız bir `.xsd` olarak yayınlanmıyor, WSDL'in `<types>` bloğunda
  gömülü geliyor. Runtime doğrulama ayrı bir şema dosyası gerektirdiğinden ilgili
  blok `src/main/resources/xsd/checkVat.xsd` dosyasına çıkarıldı.

- Exception classlarının amaçları şöyle: 
- TemporaryException tekrar denenmek üzere,
- TechnicalException tekrar denenmeyecek, loglanacak ve kullanıcıya hata mesajı dönecek,
- InvalidInputException validation hatası için, kullanıcıya hata mesajı dönecek.

- ViesFaultCode içinde contains kullanılmasının sebebi esneklik sağlanması.VIES bazen kodu düz döndürüyor, bazen uzun açıklama metninin içine gömebiliyormuş. Bu nedenle tercih ettim.

- Her iki KDV numarası da (alıcı,satıcı) her durumda sorgulanıyor. Satıcı numarası geçersiz çıksa bile alıcı sorgusu atlanmıyor. Kullanıcı tek istekte her iki tarafın durumunu da görüyor, hatalı fatura için iki kez denemek zorunda kalmaması için.
- Valid false olsa bile bu bir hata değildir, iş sonucudur. Exception verilemden devam ediliyor. 
- Akışta exception fırlatılmadan devam ediliyor ve NOT_ISSUABLE olarak işaretleniyor. Bu ayrım ViexClient içinde yapılıyor. InvoiceValidationService içinde yalnızca iş kuralları var.
- Fatura kesilemezse de 200 döndürüyorum çünkü bu bir hata değil, iş akışıdır. Not_Issuable durumu olarak işaretleniyor.

- Mapper paketi açtım ancak mapstruct kütüphanesi kullanmadım. Ekstra dependency eklememek için basit şekilde oluşturdum.
- Retryable anotasyonu proxy üzerinden çalıştığı için aynı transactional methodu gibi aynı service methodu içinde çağrıldığında retryable çalışmıyor.  Mevcut akışta `InvoiceValidationService` üzerinden `ViesClient` çağrısı bu şartı sağlıyor.
- Retry parametreleri application.yml da vies.retry altında tanımlı ancak @Retryable anotasyonuna doğrudan bağlanmadı. Anotasyon değerleri derleme zamanında sabit olmak zorunda. Configten okuma için RetryTemplate beani kurulabilirdi.
- Name ve Address alanları dönen cevapta --- olarak geliyor.

## Test
`ViesClientTest` — `MockWebServiceServer` ile gerçek servise gitmeden SOAP
akışının tamamını test ediyor. Geçerli cevabın parse edilmesi, valid false durumunda exception
fırlatılmaması, INVALID_INPUT ve MS_UNAVAILABLE faultlarının doğru
sınıflandırılması, şema ihlalinin tespit edilmesi.

`InvoiceValidationServiceTest` — `ViesClient` mock'lanarak karar mantığı test
ediliyor. Geçerli/Geçersiz kombinasyonları, doğru red gerekçesi, iki tarafın da
sorgulandığının doğrulanması, teknik hataların yukarı geçmesi.

## Uygulamayı bitirdikten sonra postman üzerinden manuel testler yaptım: 

- Geçerli numaralar → 200, ISSUABLE
- Kayıtlı olmayan numara → 200, NOT_ISSUABLE (valid=false, iş sonucu)
- Küçük harf ülke kodu ("de") → 400, bean validation, servise hiç gitmiyor
- Tanımsız ülke kodu ("XX") → 400, VIES'ten INVALID_INPUT fault'u
- Erişilemeyen endpoint → 3 deneme sonrası 503

- Son iki madde ödevde ayrılması istenen iki durumu gösteriyor, valid=false
  normal bir cevap, INVALID_INPUT ise SOAP fault.