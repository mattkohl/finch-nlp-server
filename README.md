# Finch NLP server

A basic Core NLP pipeline served with [Finch](https://github.com/finagle/finch). It exposes the following endpoints:

```
GET /sentences
POST /sentences
DELETE /sentences/<id>
DELETE /sentences
```

Upon receiving a sentence through `POST`, the service will attempt to tokenize, tag, and dependency-parse the input. 

A successful response will include a list of tokens and parse tree. For example, this request using [HTTPie](https://httpie.org/):

```bash
http POST :8081/sentences text="This is a dog."
```

would return the following:

```json
{
    "id": "24205ce0-39cb-470c-8079-eeaa12570b09",
    "parseTree": "(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (DT a) (NN dog))) (. .)))",
    "text": "This is a dog.",
    "tokens": [
        {
            "partOfSpeech": "DT",
            "token": "This"
        },
        {
            "partOfSpeech": "VBZ",
            "token": "is"
        },
        {
            "partOfSpeech": "DT",
            "token": "a"
        },
        {
            "partOfSpeech": "NN",
            "token": "dog"
        },
        {
            "partOfSpeech": ".",
            "token": "."
        }
    ]
}
```

### Run with Docker
Thanks to the [SBT Native Packager](https://github.com/sbt/sbt-native-packager), we can build this code into a Docker image:

```bash
sbt docker:publishLocal
```

To run the service inside a container:
```bash
docker run --rm -p8081:8081 finch-nlp-server:0.1  
```

