// THIS IS AN AUTOMATICALLY GENERATED FILE.  DO NOT MODIFY
// BY HAND!!
//
// Generated by lcm-gen

#include <stdint.h>
#include <stdlib.h>
#include <lcm/lcm_coretypes.h>
#include <lcm/lcm.h>

#ifndef _idsc_BinaryBlob_h
#define _idsc_BinaryBlob_h

#ifdef __cplusplus
extern "C" {
#endif

typedef struct _idsc_BinaryBlob idsc_BinaryBlob;
struct _idsc_BinaryBlob
{
    int32_t    data_length;
    int8_t     *data;
};

/**
 * Create a deep copy of a idsc_BinaryBlob.
 * When no longer needed, destroy it with idsc_BinaryBlob_destroy()
 */
idsc_BinaryBlob* idsc_BinaryBlob_copy(const idsc_BinaryBlob* to_copy);

/**
 * Destroy an instance of idsc_BinaryBlob created by idsc_BinaryBlob_copy()
 */
void idsc_BinaryBlob_destroy(idsc_BinaryBlob* to_destroy);

/**
 * Identifies a single subscription.  This is an opaque data type.
 */
typedef struct _idsc_BinaryBlob_subscription_t idsc_BinaryBlob_subscription_t;

/**
 * Prototype for a callback function invoked when a message of type
 * idsc_BinaryBlob is received.
 */
typedef void(*idsc_BinaryBlob_handler_t)(const lcm_recv_buf_t *rbuf,
             const char *channel, const idsc_BinaryBlob *msg, void *userdata);

/**
 * Publish a message of type idsc_BinaryBlob using LCM.
 *
 * @param lcm The LCM instance to publish with.
 * @param channel The channel to publish on.
 * @param msg The message to publish.
 * @return 0 on success, <0 on error.  Success means LCM has transferred
 * responsibility of the message data to the OS.
 */
int idsc_BinaryBlob_publish(lcm_t *lcm, const char *channel, const idsc_BinaryBlob *msg);

/**
 * Subscribe to messages of type idsc_BinaryBlob using LCM.
 *
 * @param lcm The LCM instance to subscribe with.
 * @param channel The channel to subscribe to.
 * @param handler The callback function invoked by LCM when a message is received.
 *                This function is invoked by LCM during calls to lcm_handle() and
 *                lcm_handle_timeout().
 * @param userdata An opaque pointer passed to @p handler when it is invoked.
 * @return 0 on success, <0 if an error occured
 */
idsc_BinaryBlob_subscription_t* idsc_BinaryBlob_subscribe(lcm_t *lcm, const char *channel, idsc_BinaryBlob_handler_t handler, void *userdata);

/**
 * Removes and destroys a subscription created by idsc_BinaryBlob_subscribe()
 */
int idsc_BinaryBlob_unsubscribe(lcm_t *lcm, idsc_BinaryBlob_subscription_t* hid);

/**
 * Sets the queue capacity for a subscription.
 * Some LCM providers (e.g., the default multicast provider) are implemented
 * using a background receive thread that constantly revceives messages from
 * the network.  As these messages are received, they are buffered on
 * per-subscription queues until dispatched by lcm_handle().  This function
 * how many messages are queued before dropping messages.
 *
 * @param subs the subscription to modify.
 * @param num_messages The maximum number of messages to queue
 *  on the subscription.
 * @return 0 on success, <0 if an error occured
 */
int idsc_BinaryBlob_subscription_set_queue_capacity(idsc_BinaryBlob_subscription_t* subs,
                              int num_messages);

/**
 * Encode a message of type idsc_BinaryBlob into binary form.
 *
 * @param buf The output buffer.
 * @param offset Encoding starts at this byte offset into @p buf.
 * @param maxlen Maximum number of bytes to write.  This should generally
 *               be equal to idsc_BinaryBlob_encoded_size().
 * @param msg The message to encode.
 * @return The number of bytes encoded, or <0 if an error occured.
 */
int idsc_BinaryBlob_encode(void *buf, int offset, int maxlen, const idsc_BinaryBlob *p);

/**
 * Decode a message of type idsc_BinaryBlob from binary form.
 * When decoding messages containing strings or variable-length arrays, this
 * function may allocate memory.  When finished with the decoded message,
 * release allocated resources with idsc_BinaryBlob_decode_cleanup().
 *
 * @param buf The buffer containing the encoded message
 * @param offset The byte offset into @p buf where the encoded message starts.
 * @param maxlen The maximum number of bytes to read while decoding.
 * @param msg Output parameter where the decoded message is stored
 * @return The number of bytes decoded, or <0 if an error occured.
 */
int idsc_BinaryBlob_decode(const void *buf, int offset, int maxlen, idsc_BinaryBlob *msg);

/**
 * Release resources allocated by idsc_BinaryBlob_decode()
 * @return 0
 */
int idsc_BinaryBlob_decode_cleanup(idsc_BinaryBlob *p);

/**
 * Check how many bytes are required to encode a message of type idsc_BinaryBlob
 */
int idsc_BinaryBlob_encoded_size(const idsc_BinaryBlob *p);

// LCM support functions. Users should not call these
int64_t __idsc_BinaryBlob_get_hash(void);
uint64_t __idsc_BinaryBlob_hash_recursive(const __lcm_hash_ptr *p);
int     __idsc_BinaryBlob_encode_array(void *buf, int offset, int maxlen, const idsc_BinaryBlob *p, int elements);
int     __idsc_BinaryBlob_decode_array(const void *buf, int offset, int maxlen, idsc_BinaryBlob *p, int elements);
int     __idsc_BinaryBlob_decode_array_cleanup(idsc_BinaryBlob *p, int elements);
int     __idsc_BinaryBlob_encoded_array_size(const idsc_BinaryBlob *p, int elements);
int     __idsc_BinaryBlob_clone_array(const idsc_BinaryBlob *p, idsc_BinaryBlob *q, int elements);

#ifdef __cplusplus
}
#endif

#endif
