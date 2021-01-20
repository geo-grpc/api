# stac-proto

## Install

```bash
$ cargo build

# to rebuild proto modules
REBUILD=true cargo build
# then in the `geometry`, `query` and `stac` modules,
# add the line `use serde::{Deserialize, Serialize};` to the other imports.
```

See `tests/mod.rs` for a full example using the client, though this will
 require a valid authentication token.
