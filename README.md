# elastic-recurring-plugin

Allow to work in ES with some features of recurrent dates defined in [rfc2445](https://www.ietf.org/rfc/rfc2445.txt). 
This plugin adds a new type named *recurring* and the native scripts: *nextOccurrence*, *hasOccurrencesAt*, *occurBetween* and *isOccurring*.

It was tested in ES 2.3.5 and 2.4.1.

## Getting start

### Compiling and installing plugin
 
Generating zip file, execute command bellow, the file will be created in folder `target\releases`.

```$mvn clean package```

To installing plugin in elasticsearch, run this command in your elasticsearch server.

```$bin/plugin install recurring-plugin-0.1.zip```

## Recurring Type
Mapper type called _recurring_ to support recurrents dates. The declaration looks as follows:
```
{
    "event" : {
        "properties" : {
            "recurrent_date" : {
                "type" : "recurring"
            }
        }
    }
}
```
The above mapping defines a _recurring_, which accepts the follow format:
```
{
    "event" : {
        "recurrent_date" : {
            "start_date" : "2016-12-25",
            "end_date" : null,
            "rrule": "RRULE:FREQ=YEARLY;BYMONTH=12;BYMONTHDAY=25"
        }
    }
}
```

## Native scripts

### nextOccurrence

Script field returns date of next occurrence of event in yyyy-MM-dd.

*Parameters:*
- *field* - Name of property, type must be _recurring_.

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

### isOccurring

Script field returns `true` if event is occurring considering server date.

*Parameters:*  
- *field* - Name of property, type must be _recurring_.


### Samples

## Adding a mapping

PUT `sample/event/_mapping`
```
{
  "event": {
    "properties": {
      "name": {
        "type": "string"
      },
      "recurrent_date": {
        "type": "recurring"
      }
    }
  }
}
```

## Adding data

PUT `sample/event/1`
```
{
  "name": "Christmas",
  "recurrent_date": {
    "start_date": "2015-12-25",
    "rrule": "RRULE:FREQ=YEARLY;BYMONTH=12;BYMONTHDAY=25;WKST=SU"
  }
}
```

PUT `sample/event/2`
```
{
  "name": "Mother's day",
  "recurrent_date": {
    "start_date": "2016-05-08",
    "rrule": "RRULE:FREQ=YEARLY;BYMONTH=5;BYDAY=2SU;WKST=SU"
  }
}
```

PUT `sample/event/3`
```
{
  "name": "Halloween",
  "recurrent_date": {
    "start_date": "2012-10-31",
    "rrule": "RRULE:FREQ=YEARLY;BYMONTH=10;BYMONTHDAY=31;WKST=SU"
  }
}
```

PUT `sample/event/4`
```
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

POST `sample/event/_search`
```
{
  "fields": {
    "fields": [
      "name"
    ],
    "script_fields": {
      "nextOccur": {
        "script": "nextOccurrence",
        "lang": "native",
        "params": {
          "field": "recurrent_date"
        }
      }
    }
  }
}
```
RESPONSE
```
{
  "took": 9,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 4,
    "max_score": 1,
    "hits": [
      {
        "_index": "sample",
        "_type": "event",
        "_id": "3",
        "_score": 1,
        "fields": {
          "name": [
            "Halloween"
          ],
          "nextOccur": [
            "2017-10-31"
          ]
        }
      },
      {
        "_index": "sample",
        "_type": "event",
        "_id": "2",
        "_score": 1,
        "fields": {
          "name": [
            "Mother's day"
          ],
          "nextOccur": [
            "2017-05-14"
          ]
        }
      },
      ...
    ]
  }
}
```

### hasOccurrencesAt

POST `sample/event/_search`
```
{
  "query": {
    "bool": {
      "filter": {
        "script": {
          "script": "hasOccurrencesAt",
          "lang": "native",
          "params": {
            "field": "recurrent_date",
            "date": "2019-05-12"
          }
        }
      }
    }
  }
}
``` 
RESPONSE
```
{
  "took": 8,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 1,
    "max_score": 0,
    "hits": [
      {
        "_index": "sample",
        "_type": "event",
        "_id": "2",
        "_score": 0,
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

POST `sample/event/_search`
```
{
  "query": {
    "bool": {
      "filter": {
        "script": {
          "script": "occurBetween",
          "lang": "native",
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
``` 
RESPONSE
```
{
  "took": 8,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 2,
    "max_score": 0,
    "hits": [
      {
        "_index": "sample",
        "_type": "event",
        "_id": "2",
        "_score": 0,
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
        "_type": "event",
        "_id": "4",
        "_score": 0,
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

### isOccurring

POST `sample/event/_search`
```
{
  "query": {
    "bool": {
      "filter": {
        "script": {
          "script": "isOccurring",
          "lang": "native",
          "params": {
            "field": "recurrent_date"
          }
        }
      }
    }
  }
}
``` 
RESPONSE
```
{
  "took": 9,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "hits": {
    "total": 3,
    "max_score": 0,
    "hits": [
      {
        "_index": "sample",
        "_type": "event",
        "_id": "3",
        "_score": 0,
        "_source": {
          "name": "Halloween",
          "recurrent_date": {
            "start_date": "2012-10-31",
            "rrule": "RRULE:FREQ=YEARLY;BYMONTH=10;BYMONTHDAY=31;WKST=SU"
          }
        }
      },
      {
        "_index": "sample",
        "_type": "event",
        "_id": "2",
        "_score": 0,
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
        "_type": "event",
        "_id": "1",
        "_score": 0,
        "_source": {
          "name": "Christmas",
          "recurrent_date": {
            "start_date": "2015-12-25",
            "rrule": "RRULE:FREQ=YEARLY;BYMONTH=12;BYMONTHDAY=25;WKST=SU"
          }
        }
      }
    ]
  }
}

```