#[cfg(test)]
mod tests {
    use futures03::{compat::*, pin_mut, task::Poll, Stream};
    use futures03_test::{stream::StreamTestExt, task::noop_context};
    use grpcio::{ChannelBuilder, EnvBuilder, Error};
    use stac_proto::*;

    #[test]
    fn it_works() -> Result<(), Error> {
        let env = std::sync::Arc::new(EnvBuilder::new().build());
        let channel = ChannelBuilder::new(env).connect("https://eap.nearspacelabs.net");
        let client = StacServiceClient::new(channel);

        let res_stream = client
            .search(&StacRequest::default())?
            .compat()
            .interleave_pending();

        pin_mut!(res_stream);
        let mut ctx = noop_context();

        let first = res_stream.as_mut().poll_next(&mut ctx);
        assert!(first.is_pending());

        match res_stream.as_mut().poll_next(&mut ctx) {
            Poll::Ready(Some(Ok(stac_item))) => {
                println!("stac item: {:?}", stac_item);
                assert!(true);
            }
            Poll::Ready(Some(Err(err))) => {
                assert!(false, format!("result stream produced error: {}", err));
            }
            Poll::Pending => assert!(false, "result stream still pending"),
            _ => assert!(false, "result stream errored out"),
        };
        Ok(())
        //        println!("resulting stac item: {:?}", res.unwrap());
    }
}
