<script setup>
import { ref } from 'vue';

const emit = defineEmits(['update:image', 'upload-status']);

const imageFile = ref(null);
const previewUrl = ref('');
const status = ref('idle');
const error = ref(null);

const MAX_FILE_SIZE = 1024 * 1024 * 2; // 2MB max size
const TARGET_WIDTH = 800; // Target width for resized image

const handleImageSelection = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  status.value = 'processing';
  error.value = null;

  if (!file.type.match('image.*')) {
    error.value = 'Please select an image file';
    status.value = 'error';
    emit('upload-status', { status: 'error', message: 'Invalid file type' });
    return;
  }

  try {
    // Clear previous preview
    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value);
      previewUrl.value = '';
    }

    // Resize and compress the image
    const compressedImage = await resizeAndCompressImage(file);
    imageFile.value = compressedImage;

    // Create a temporary URL for preview
    previewUrl.value = URL.createObjectURL(compressedImage);

    status.value = 'ready';
    emit('update:image', compressedImage);
    emit('upload-status', { status: 'ready', message: 'Image ready' });
  } catch (err) {
    console.error('Image processing error:', err);
    error.value = 'Failed to process image';
    status.value = 'error';
    emit('upload-status', { status: 'error', message: err.message });
  } finally {
    // Clear the input to allow selecting the same file again
    event.target.value = null;
  }
};

const resizeAndCompressImage = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = (event) => {
      const img = new Image();

      img.onload = () => {
        // Calculate new dimensions preserving aspect ratio
        let width = img.width;
        let height = img.height;

        if (width > TARGET_WIDTH) {
          height = (TARGET_WIDTH / width) * height;
          width = TARGET_WIDTH;
        }

        // Create canvas for resizing
        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;

        // Draw and resize image on canvas
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, width, height);

        // Convert to Blob with compression
        canvas.toBlob((blob) => {
          if (!blob) {
            reject(new Error('Failed to compress image'));
            return;
          }

          // If still too large, try more compression
          if (blob.size > MAX_FILE_SIZE) {
            canvas.toBlob((smallerBlob) => {
              if (!smallerBlob) {
                reject(new Error('Failed to compress image further'));
                return;
              }

              // Create a new file from the blob
              const compressedFile = new File([smallerBlob], file.name, {
                type: 'image/jpeg',
                lastModified: Date.now()
              });

              resolve(compressedFile);
            }, 'image/jpeg', 0.7); // Higher compression
          } else {
            // Create a new file from the blob
            const compressedFile = new File([blob], file.name, {
              type: 'image/jpeg',
              lastModified: Date.now()
            });

            resolve(compressedFile);
          }
        }, 'image/jpeg', 0.85); // Initial compression quality
      };

      img.onerror = () => {
        reject(new Error('Failed to load image'));
      };

      img.src = event.target.result;
    };

    reader.onerror = () => {
      reject(new Error('Failed to read file'));
    };

    reader.readAsDataURL(file);
  });
};

const removeImage = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value);
  }

  imageFile.value = null;
  previewUrl.value = '';
  status.value = 'idle';
  emit('update:image', null);
  emit('upload-status', { status: 'idle', message: 'Image removed' });
};
</script>

<template>
  <div class="image-uploader">
    <div v-if="!imageFile" class="upload-zone">
      <input
          id="camera-input"
          type="file"
          accept="image/*"
          capture="environment"
          @change="handleImageSelection"
          class="file-input"
      />
      <label for="camera-input" class="upload-btn">
        <span>Take Photo</span>
      </label>
    </div>

    <div v-else class="preview-zone">
      <img v-if="previewUrl" :src="previewUrl" alt="Preview" class="image-preview" />
      <div class="preview-actions">
        <button type="button" class="remove-btn" @click="removeImage">
          Remove
        </button>
        <div class="status-indicator">
          <span v-if="status === 'processing'" class="processing">Processing...</span>
          <span v-else-if="status === 'ready'" class="ready">Ready ✓</span>
          <span v-else-if="status === 'error'" class="error">{{ error }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.image-uploader {
  width: 100%;
  margin-bottom: 15px;
}

.upload-zone {
  display: flex;
  justify-content: center;
  width: 100%;
}

.file-input {
  display: none;
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border: 1px dashed #ccc;
  border-radius: 4px;
  padding: 12px 20px;
  cursor: pointer;
  width: 100%;
  transition: background-color 0.2s;
}

.upload-btn:active {
  background: #e0e0e0;
}

.preview-zone {
  width: 100%;
}

.image-preview {
  width: 100%;
  height: auto;
  max-height: 200px;
  object-fit: contain;
  border-radius: 4px;
  margin-bottom: 8px;
  border: 1px solid #ddd;
}

.preview-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.remove-btn {
  background: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
}

.status-indicator {
  font-size: 14px;
}

.processing {
  color: #2196F3;
}

.ready {
  color: #4CAF50;
}

.error {
  color: #f44336;
}
</style>