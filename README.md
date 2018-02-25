# Finch NLP server

A basic Core NLP pipeline served with [Finch](https://github.com/finagle/finch). It exposes the following endpoints:

```
GET /jobs
POST /jobs
DELETE /jobs/<id>
DELETE /jobs
```

Upon receiving a job through `POST`, the service will attempt to tokenize, tag, and dependency-parse the `text` input. 

A successful response will include a list of tokens and parse tree. For example, this request using [HTTPie](https://httpie.org/):

```bash
http POST :8081/jobs text="It was all a dream."
```

would return the following:

```json
{
    "id": "6d34b552-c075-489f-8c6e-c4f5f58b18a8",
    "parseTrees": [
        "(ROOT (S (NP (PRP It)) (VP (VBD was) (NP (PDT all) (DT a) (NN dream))) (. .)))"
    ],
    "text": "It was all a dream.",
    "tokens": [
        {
            "partOfSpeech": "PRP",
            "token": "It"
        },
        {
            "partOfSpeech": "VBD",
            "token": "was"
        },
        {
            "partOfSpeech": "PDT",
            "token": "all"
        },
        {
            "partOfSpeech": "DT",
            "token": "a"
        },
        {
            "partOfSpeech": "NN",
            "token": "dream"
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

