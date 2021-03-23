# Talend Open Studio for ESB


[![Visit Talend's website](https://www.talend.com/wp-content/uploads/2016/07/talend-logo.png "Talend")](http://www.talend.com/)


This repository contains the source files for Talend Open Studio for ESB.


## Repository Structure

All Talend Studio repositories follow the same file structure:

```

  |_ main          Main Eclipse plugins and features
    |_ features
    |_ plugins
  |_ test          Eclipse plugins and features for unit tests.
      |_ features
      |_ plugins
  |_ i18n          Internationalization plugins and features.
      |_ features
      |_ plugins
```

## How to build projects

Follow the instructions at [Talend/studio-se-master](https://github.com/Talend/studio-se-master) and run the following from inside the `studio-se-master` repository.

```
mvn clean install \
    -Dtos.esb=true \
    -Dtos.bd=false \
    -Dtos.di=false \
    -Dtos.dq=false \
    -Dtos.mdm=false
```

## Download

You can download this product from the [Talend website](http://www.talend.com/download/talend-open-studio?qt-product_tos_download_new=3&utm_medium=communityext&utm_source=github&utm_campaign=tosesb).


## Usage and Documentation

Documentation is available on [Talend Help Center](http://help.talend.com/).


## Support

You can ask for help on our [Forum](http://www.talend.com/services/technical-support).


## Contributing

We welcome contributions of all kinds from anyone.

Using the [Talend bugtracker](http://jira.talendforge.org/) is the best channel for bug reports and feature requests. Use GitHub to submit pull requests.

Feel free to share your Talend components on [Talend Exchange](http://www.talendforge.org/exchange).

## License

Copyright (c) 2006-2021 Talend

Licensed under the Apache V2 License
