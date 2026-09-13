// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import S3rver from 's3rver';
import {S3Client, CreateBucketCommand, PutObjectCommand, GetObjectCommand} from '@aws-sdk/client-s3';
const directory = await fs.mkdtemp(path.join(os.tmpdir(), 'foresight-s3-'));
const server = new S3rver({port: 0, address: '127.0.0.1', directory, silent: true});
let client;
try {
  const {port} = await server.run();
  client = new S3Client({endpoint: `http://127.0.0.1:${port}`, region: 'us-east-1', forcePathStyle: true,
    credentials: {accessKeyId: 'S3RVER', secretAccessKey: 'S3RVER'}});
  await client.send(new CreateBucketCommand({Bucket: 'foresight-local'}));
  await client.send(new PutObjectCommand({Bucket: 'foresight-local', Key: 'draft.edn', Body: '{:status :draft}'}));
  const result = await client.send(new GetObjectCommand({Bucket: 'foresight-local', Key: 'draft.edn'}));
  assert.equal(await result.Body.transformToString(), '{:status :draft}');
  console.log('PASS local S3 create/put/get round trip');
} finally {
  client?.destroy();
  await server.close();
  await fs.rm(directory, {recursive: true, force: true});
}
