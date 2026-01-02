CREATE TABLE IF NOT EXISTS api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_id VARCHAR(16) UNIQUE NOT NULL,
    client_id UUID NOT NULL REFERENCES clients(id),
    hashed_key TEXT NOT NULL,
    usage_count INTEGER DEFAULT 0,
    usage_limit INTEGER NOT NULL DEFAULT 1000,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW());
--;;
CREATE INDEX idx_api_keys_key_id ON api_keys(key_id);
