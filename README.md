## ASN Resolver

Asn resolver provides grpc service for translating IP address to [ASN](https://en.wikipedia.org/wiki/Autonomous_system_(Internet)).

### Usage

Pull docker image

```bash
docker pull ghcr.io/flowguard/asn-resolver:latest
```

Run docker image with these env variables: 

* `ASN_PROVIDER` - Provider of the ASN database. At this time only `geolite` [asn database source](https://dev.maxmind.com/geoip/docs/databases/asn?lang=en) is supported
* `ASN_DB_REFRESH_RATE` - ASN database refresh period eg. `24 hours`
* `GEOLITE_API_KEY` - [Api key](https://support.maxmind.com/hc/en-us/articles/4407111582235-Generate-a-License-Key) used for geolite db access 

```bash
docker run -p 8090:8090 -e GEOLITE_API_KEY=<api-key> -e ASN_PROVIDER="geolite" -e ASN_DB_REFRESH_RATE="24 hours" ghcr.io/flowguard/asn-resolver:latest
```

Test it with `grpcurl` tool or with arbitrary grpc client

```bash
$ grpcurl -plaintext -d '{"ip_address": "1.1.1.1"}' localhost:8090 asnservice.AsnService.GetAsnNum                                                                   
{
  "asnNum": 13335,
  "asnName": "CLOUDFLARENET"
}

$ grpcurl -plaintext -d '{"ip_address": "192.168.1.1"}' localhost:8090 asnservice.AsnService.GetAsnNum                                                              
ERROR:
  Code: NotFound
  Message: ASN not found
```
