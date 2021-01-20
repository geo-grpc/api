#[macro_use]
extern crate lazy_static;

lazy_static! {
    pub static ref TOKEN: String = {
        let token =
            std::env::var("TOKEN").expect("Failed to retrieve environment variable `TOKEN`");
        format!("Bearer {}", token)
    };
}

#[cfg(test)]
mod tests {
    use super::TOKEN;
    use futures03::{compat::Stream01CompatExt, executor, StreamExt};
    use grpcio::{CallOption, ChannelBuilder, EnvBuilder, Error, MetadataBuilder};
    use stac_proto::*;

    #[test]
    fn it_works() -> Result<(), Error> {
        // setup
        let env = std::sync::Arc::new(EnvBuilder::new().build());
        let channel = ChannelBuilder::new(env).connect("api.nearspacelabs.net:9090");
        let client = StacServiceClient::new(channel);

        // headers
        let mut metadata_builder = MetadataBuilder::new();
        metadata_builder
            .add_str("Authorization", TOKEN.as_str())
            .unwrap_or_else(|_| panic!("unable to set token as an `Authorization` header"));
        let metadata = metadata_builder.build();
        let opts = CallOption::default().headers(metadata);

        let req = &StacRequest::default();

        // collect result stream into vec
        let res = client.search_opt(&req, opts)?.compat();
        let vec = executor::block_on(res.collect::<Vec<Result<StacItem, Error>>>());

        assert!(!vec.is_empty(), "Failed to retrieve any stac items");
        for item in vec.into_iter() {
            assert!(item.is_ok(), format!("Erroneous result: {:?}", item));
            println!("stac item ID: {:?}", item.unwrap().get_id());
        }

        Ok(())
    }
}
