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
    use futures03::{compat::Stream01CompatExt, executor, pin_mut, task::Poll, Stream, StreamExt};
    use futures03_test::{stream::StreamTestExt, task::noop_context};
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
        let opts = CallOption::default().headers(metadata_builder.build());

        // result stream into vec
        let res_stream = client.search_opt(&StacRequest::default(), opts)?.compat();
        let mut vec = executor::block_on(res_stream.collect::<Vec<Result<StacItem, Error>>>());

        assert!(!vec.is_empty(), "Failed to retrieve any stac items");
        for (count, item) in vec.into_iter().enumerate() {
            assert!(item.is_ok(), format!("Erroneous result: {:?}", item));
            println!("stac item ID: {:?}", item.unwrap().get_id());
        }

        Ok(())
    }
}
