***Build***


Use **docker_image_build_and_deployment_script.sh** either directly, if your system supports it (Linux for example) or something similar on other systems.
Make sure you have Docker installed, as the build for AWS Lmabda, for compatibility reasons, is done in Amazon Linux 2 environment (container).



***Deployment***


Just create AWS LAmbda function from scratch, select Amazon Linux 2 runtime, give it name and then upload zip with binary, renamed to "bootstrap", this app should echo any request.
