# elastic-recurring-plugin

Allow to work in ES with some features of recurrent dates defined in [rfc2445](https://www.ietf.org/rfc/rfc2445.txt). 
This plugin adds a new type named *recurring* and the native scripts: *nextOccurrence*, *hasOccurrencesAt*, *occurBetween* and *notHasExpired*.

[![Build Status](https://travis-ci.org/betorcs/elastic-recurring-plugin.svg?branch=7.7)](https://travis-ci.org/betorcs/elastic-recurring-plugin)

## Getting start

### Compiling and installing plugin
 
Generating zip file, execute command bellow, the file will be created in folder `target\releases`.

```bash
./gradlew clean build -x test
```

To installing plugin in elasticsearch, run this command in your elasticsearch server.

```bash
./bin/elasticsearch-plugin install https://github.com/betorcs/elastic-recurring-plugin/archive/elastic-recurring-plugin-7.7.zip
```

## Recurring Type
Mapper type called _recurring_ to support recurrents dates. The declaration looks as follows:
```json
{
    "properties": {
        "name": {
            "type": "text"
        },
        "recurrent_date": {
            "type": "recurring"
        }
    }
}
```
The above mapping defines a _recurring_, which accepts the follow format:
```json
{
    "_index": "sample",
    "_type": "_doc",
    "_id": "1",
    "_score": 1.0,
    "_source": {
        "name": "Christmas",
        "recurrent_date": {
            "start_date": "2015-12-25",
            "rrule": "RRULE:FREQ=YEARLY;BYMONTH=12;BYMONTHDAY=25;WKST=SU"
        }
    }
}
```

## Native scripts

### nextOccurrence

Script field returns date of next occurrence of event in yyyy-MM-dd when it's recurrent, 
and the `start_date` when start/end is valid yet, to both situation today'll be considered if `from` parameter is omitted.

*Parameters:*
- *field* - Name of property, type must be _recurring_.
- *from* - Optional, date to be considered _from_.

### hasOccurrencesAt

Script field returns `true` if event occurrs in determinated date.

*Parameters:*
- *field* - Name of property, type must be _recurring_.
- *date* - Date 

### occurBetween

Script field returns `true` if event occurrs in determinated range of date.

*Parameters:*  
- *field* - Name of property, type must be _recurring_.
- *start* - Starting date inclusive.
- *end* - Ending date inclusive.

### notHasExpired

Script field returns `true` if event is not expired considering server date.

*Parameters:*  
- *field* - Name of property, type must be _recurring_.

### Samples

## Adding a mapping

```http request
PUT localhost:9200/sample/_mapping
Content-Type: application/json

{
    "properties": {
        "name": {
            "type": "text"
        },
        "recurrent_date": {
            "type": "recurring"
        }
    }
}
```

## Adding data

```http request
PUT localhost:9200/sample/_doc/1
Content-Type: application/json

{
  "name": "Christmas",
  "recurrent_date": {
    "start_date": "2015-12-25",
    "rrule": "RRULE:FREQ=YEARLY;BYMONTH=12;BYMONTHDAY=25;WKST=SU"
  }
}
```

```http request
PUT localhost:9200/sample/_doc/2
Content-Type: application/json

{
  "name": "Mother's day",
  "recurrent_date": {
    "start_date": "2016-05-08",
    "rrule": "RRULE:FREQ=YEARLY;BYMONTH=5;BYDAY=2SU;WKST=SU"
  }
}
```

```http request
PUT localhost:9200/sample/_doc/3
Content-Type: application/json

{
  "name": "Halloween",
  "recurrent_date": {
    "start_date": "2012-10-31",
    "rrule": "RRULE:FREQ=YEARLY;BYMONTH=10;BYMONTHDAY=31;WKST=SU"
  }
}
```

```http request
PUT localhost:9200/sample/_doc/4
Content-Type: application/json

{
  "name": "5 maintenance monthly of cruze ",
  "recurrent_date": {
    "start_date": "2016-03-10",
    "rrule": "RRULE:FREQ=MONTHLY;BYMONTHDAY=10;COUNT=5;WKST=SU"
  }
}
``` 


## Using scripts

### nextOccurrence

```http request
POST localhost:9200/sample/_search
Content-Type: application/json

{
    "_source": ["name"],
    "script_fields": {
        "nextOccur": {
            "script": {
                "source": "nextOccurrence",
                "lang": "recurring_scripts",
                "params": {
                    "field": "recurrent_date"
                }   
            }
        }
    }
}
```

RESPONSE
```json
{
  "took": 8,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 4,
      "relation": "eq"
    },
    "max_score": 1.0,
    "hits": [
      {
        "_index": "sample",
        "_type": "_doc",
        "_id": "1",
        "_score": 1.0,
        "_source": {
          "name": "Christmas"
        },
        "fields": {
          "nextOccur": [
            "2020-12-25"
          ]
        }
      },
      {
        "_index": "sample",
        "_type": "_doc",
        "_id": "2",
        "_score": 1.0,
        "_source": {
          "name": "Mother's day"
        },
        "fields": {
          "nextOccur": [
            "2021-05-09"
          ]
        }
      },
      ...
    ]
  }
}
```

### hasOccurrencesAt

```http request
POST localhost:9200/sample/_search
Content-Type: application/json

{
    "query": {
        "bool": {
            "filter": {
                "script": {
                    "script": {
                        "source": "hasOccurrencesAt",
                        "lang": "recurring_scripts",
                        "params": {
                            "field": "recurrent_date",
                            "date": "2019-05-12"
                        }   
                    }
                }
            }            
        }
    }
}
```
 
RESPONSE
```json
{
  "took": 21,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 1,
      "relation": "eq"
    },
    "max_score": 0.0,
    "hits": [
      {
        "_index": "sample",
        "_type": "_doc",
        "_id": "2",
        "_score": 0.0,
        "_source": {
          "name": "Mother's day",
          "recurrent_date": {
            "start_date": "2016-05-08",
            "rrule": "RRULE:FREQ=YEARLY;BYMONTH=5;BYDAY=2SU;WKST=SU"
          }
        }
      }
    ]
  }
}
```


### occurBetween

```http request
POST localhost:9200/sample/_search
Content-Type: application/json

{
    "query": {
        "bool": {
            "filter": {
                "script": {
                    "script": {
                        "source": "occurBetween",
                        "lang": "recurring_scripts",
                        "params": {
                            "field": "recurrent_date",
                            "start": "2016-01-31",
                            "end": "2016-07-26"
                        }
                    }
                }
            }            
        }
    }
}
``` 
RESPONSE
```json
{
  "took": 11,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 2,
      "relation": "eq"
    },
    "max_score": 0.0,
    "hits": [
      {
        "_index": "sample",
        "_type": "_doc",
        "_id": "2",
        "_score": 0.0,
        "_source": {
          "name": "Mother's day",
          "recurrent_date": {
            "start_date": "2016-05-08",
            "rrule": "RRULE:FREQ=YEARLY;BYMONTH=5;BYDAY=2SU;WKST=SU"
          }
        }
      },
      {
        "_index": "sample",
        "_type": "_doc",
        "_id": "4",
        "_score": 0.0,
        "_source": {
          "name": "5 maintenance monthly of cruze ",
          "recurrent_date": {
            "start_date": "2016-03-10",
            "rrule": "RRULE:FREQ=MONTHLY;BYMONTHDAY=10;COUNT=5;WKST=SU"
          }
        }
      }
    ]
  }
}
```

### notHasExpired

```http request
POST localhost:9200/sample/_search
Content-Type: application/json

{
    "query": {
        "bool": {
            "filter": {
                "script": {
                    "script": {
                        "source": "notHasExpired",
                        "lang": "recurring_scripts",
                        "params": {
                            "field": "recurrent_date"
                        }
                    }
                }
            }            
        }
    }
}
```
 
RESPONSE
```json
{
  "took": 14,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 3,
      "relation": "eq"
    },
    "max_score": 0.0,
    "hits": [
      {
        "_index": "sample",
        "_type": "_doc",
        "_id": "1",
        "_score": 0.0,
        "_source": {
          "name": "Christmas",
          "recurrent_date": {
            "start_date": "2015-12-25",
            "rrule": "RRULE:FREQ=YEARLY;BYMONTH=12;BYMONTHDAY=25;WKST=SU"
          }
        }
      },
      {
        "_index": "sample",
        "_type": "_doc",
        "_id": "2",
        "_score": 0.0,
        "_source": {
          "name": "Mother's day",
          "recurrent_date": {
            "start_date": "2016-05-08",
            "rrule": "RRULE:FREQ=YEARLY;BYMONTH=5;BYDAY=2SU;WKST=SU"
          }
        }
      },
      {
        "_index": "sample",
        "_type": "_doc",
        "_id": "3",
        "_score": 0.0,
        "_source": {
          "name": "Halloween",
          "recurrent_date": {
            "start_date": "2012-10-31",
            "rrule": "RRULE:FREQ=YEARLY;BYMONTH=10;BYMONTHDAY=31;WKST=SU"
          }
        }
      }
    ]
  }
}
```
